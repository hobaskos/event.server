#!/bin/python
#
# simple seeder client for the API application.
#

import requests
import random
from faker import Faker


base_url = input("=> Enter host: ")
user_name = input("=> Enter admin username: ")
user_pass = input("=> Enter admin password: ")

word_file = "/usr/share/dict/cracklib-small"
words = open(word_file).read().splitlines()
fake = Faker('no_NO')

event_categories = ["Music", "Pub Crawl", "Business", "Family", "Sport",
            "Seminars", "Team Building", "Health", "Fashion", "Religion"]
event_count = 1000
user_count = 5
default_password = "password"
user_emails = []

def create_random_user():
    email = fake.email()
    return {
        "activated": True,
        "authorities": ["ROLE_USER"],
        "email": email,
        "login": email,
        "firstName": fake.name().split(" ")[0],
        "lastName": fake.name().split(" ")[1],
        "langKey": "en",
        "password": default_password,
        "profileImageUrl": "http://no-url.no"
    }

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

def create_location_payload(event, index, lat_tuple, lon_tuple, month, day, start_hour, end_hour):
    return {
        "name" : fake.address(),
        "description": fake.text(),
        "fromDate": "2017-%02d-%02dT%02d:00:00.000Z" % (month, day, start_hour),
        "toDate": "2017-%02d-%02dT%02d:00:00.000Z" % (month, day, end_hour),
        "eventId": event.json().get("id"),
        "geoPoint": {
           "lat": random.uniform(lat_tuple[0], lat_tuple[1]),
           "lon": random.uniform(lon_tuple[0], lon_tuple[1])
        }
    }

def create_locations(event):
    month = random.randrange(1,13)
    day = random.randrange(1,29)
    start_hour = 10
    end_hour = 11
    locations = []
    rand_lat = random.uniform(59.30, 60.46)
    rand_lon = random.uniform(9.56, 11.30)
    lat_tuple = (rand_lat, rand_lat + 0.05)
    lon_tuple = (rand_lon, rand_lon + 0.05)
    for i in range(0, random.randrange(1, 6)):
        locations.insert(i, create_location_payload(event, i, lat_tuple, lon_tuple, month, day, start_hour + i, end_hour + i))
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

print("# Generating users")
for u in range(1, user_count):
    generated_user = create_random_user()
    user_emails.append(generated_user["email"])
    user = requests.post(base_url + "/api/managed-users",
                         headers=jwt_header,
                         json=create_random_user())

print("# Generating data")
for cat in event_categories:
    eventCategory = requests.post(base_url + "/api/event-categories",
               headers=jwt_header,
               json=create_category_payload(cat))
    for ei in range(1, event_count):
        event = requests.post(base_url + "/api/events",
                    headers=jwt_header,
                    json=create_event_payload(eventCategory))
        locations = create_locations(event)
        for loc in locations:
            location = requests.post(base_url + "/api/locations",
                    headers=jwt_header,json=loc)
