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
fake = Faker('no_NO')

def create_category_payload(title):
    return {
        "icon": "ZGFuaw==",
        "iconContentType": "image/png",
        "theme": random.choice(["RED","ORANGE","YELLOW","GREEN","BLUE","INDIGO","VIOLET"]),
        "title": title
    }

def create_event_payload(category):
    return {
        "title": fake.sentence(2),
        "description": fake.text(),
        "eventCategory": category.json()
    }

def create_location_payload(event, index):
    return {
        "name" : fake.address(),
        "description": fake.text(),
        "fromDate": "2017-03-%02dT10:00:00.000Z" % index,
        "toDate": "2017-03-%02dT11:00:00.000Z" % index,
        "eventId": event.json().get("id"),
        "geoPoint": {
                   "lat": random.uniform(59.75, 59.95),
                   "lon": random.uniform(10.55, 10.75)
        },
        "vector": index
    }


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
    for ei in range(1,50):
        event = requests.post(base_url + "/api/events",
                    headers=jwt_header,
                    json=create_event_payload(eventCategory))
        for li in range(1,random.randrange(2,10)):
            location = requests.post(base_url + "/api/locations",
                    headers=jwt_header,json=create_location_payload(event, li))
