#!/bin/python
#
# simple seeder client for the API application.
#

import requests
import json
import uuid
import base64
import random
from faker import Faker


base_url = input("=> Enter host: ")
user_name = input("=> Enter username: ")
user_pass = input("=> Enter password: ")

word_file = "/usr/share/dict/cracklib-small"
words = open(word_file).read().splitlines()
fake = Faker('no_NO')


def random_event_title():
    return "%s %s" % \
            (random.choice(words).capitalize(), random.choice(words))

def create_category_payload(title):
    return {
        "icon": "ZGFuaw==",
        "iconContentType": "image/png",
        "theme": random.choice(["RED","ORANGE","YELLOW","GREEN","BLUE","INDIGO","VIOLET"]),
        "title": title
    }

def create_event_payload(category):
    return {
        "title": random_event_title(),
        "description": fake.text(),
        "eventCategory": category.json()
    }

def create_location_payload(event, index, month, day, start_hour, end_hour):
    return {
        "name" : fake.address(),
        "description": fake.text(),
        "fromDate": "2017-%02d-%02dT%02d:00:00.000Z" % (month, day, start_hour),
        "toDate": "2017-%02d-%02dT%02d:00:00.000Z" % (month, day, end_hour),
        "eventId": event.json().get("id"),
        "geoPoint": {
                   "lat": random.uniform(59.75, 59.95),
                   "lon": random.uniform(10.55, 10.75)
        },
        "vector": index
    }

def create_locations(event):
    month = random.randrange(1,13)
    day = random.randrange(1,29)
    start_hour = 10
    end_hour = 11
    locations = []
    for i in range(0, random.randrange(1, 10)):
        locations.insert(i, create_location_payload(event, i, month, day, start_hour + i, end_hour + i))
    return locations



print('# Authenticating user')
payload = {"username": user_name, "remember_me": True, "password": user_pass}
register = requests.post(base_url + "/api/authenticate", json=payload)

if register.status_code == 401:
  print(register.text)
  exit()

token = register.json().get("id_token")
jwt_header = {
    "Authorization": "Bearer " + token,
    "Content-Type": "application/json"
}

print("# Generating data")
for cat in ["Music", "Pub Crawl", "Business", "Family", "Sport"]:
    eventCategory = requests.post(base_url + "/api/event-categories",
               headers=jwt_header,
               json=create_category_payload(cat))
    for ei in range(1,1000):
        event = requests.post(base_url + "/api/events",
                    headers=jwt_header,
                    json=create_event_payload(eventCategory))
        locations = create_locations(event)
        for loc in locations:
            location = requests.post(base_url + "/api/locations",
                    headers=jwt_header,json=loc)
