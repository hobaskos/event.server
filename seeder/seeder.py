#!/bin/python
#
# simple seeder client for the API application.
#

import requests
import random
import glob
import base64
import os
from faker import Faker


base_url = input("=> Enter host: ")
user_name = input("=> Enter admin username: ")
user_pass = input("=> Enter admin password: ")

word_file = "/usr/share/dict/cracklib-small"
words = open(word_file).read().splitlines()
fake = Faker('no_NO')

event_categories_titles = ["Music", "Pub Crawl", "Business", "Family", "Sport",
            "Seminars", "Team Building", "Health", "Fashion", "Religion"]
event_categories = []
event_images = [
    "https://www.sitebuilderreport.com/assets/facebook-stock-up-446fff24fb11820517c520c4a5a4c032.jpg",
    "http://www.apimages.com/Images/Ap_Creative_Stock_Header.jpg",
    "https://www.ethos3.com/wp-content/uploads/2014/11/stock-photo-600x405.jpg",
    "https://g.foolcdn.com/editorial/images/245314/dice-1500_large.jpg"
]
profile_images = [
    "http://4.bp.blogspot.com/-BHhUazKytmw/VbCfWPqrOJI/AAAAAAAAB7c/qj6WVX3du-s/s1600/51b91bba5a3fd9b6c8b9c53bc0ab6c65.jpg",
    "http://silenteye.net/ui/images/16/WDF_1102495.jpg",
    "https://i1.wp.com/devilsworkshop.org/files/2013/01/enlarged-facebook-profile-picture.jpg?resize=448%2C448",
    "https://upload.wikimedia.org/wikipedia/en/7/70/Shawn_Tok_Profile.jpg",
    "https://www.engineering.cornell.edu/engineering/customcf/iws_ai_faculty_display/ai_images/caa238-profile.jpg",
    "http://sbpmv.org.in/images/photo-profile.jpg",
    "http://www.trickscity.com/wp-content/uploads/2016/07/Cool-And-Stylish-Profile-Pictures-For-Facebook-For-Girls-17-1024x993.jpg",
    "https://yt3.ggpht.com/-OHiWZDaQu3A/AAAAAAAAAAI/AAAAAAAAAAA/yvCrjK_9iR4/s900-c-k-no-mo-rj-c0xffffff/photo.jpg",
    "https://cdn.pixabay.com/photo/2013/04/07/17/57/woman-101542__340.jpg",
    "http://www.celebbra.com/wp-content/uploads/2016/01/Nayantara-Height-Weight-Bra-Pics-Profile.jpg",
    "https://justinjackson.ca/wp-content/uploads/2008/08/justin-jackson-black-and-white-canada-profile.jpg",
    "https://www2.mmu.ac.uk/media/mmuacuk/style-assets/images/r-research/profile-Zeyad.jpg",
    "http://thunder-team.com/friend-finder/images/users/user-12.jpg",
    "https://s3-media3.fl.yelpcdn.com/bphoto/YOhv2_7yGqiLEL28R4knJg/ls.jpg",
    "http://www.johnson.cornell.edu/people/faculty/jdh362/jdh362_profile.jpg",
    "https://s-media-cache-ak0.pinimg.com/originals/74/05/2f/74052f225f4bae0e308bcb0ca1407e6b.jpg",
    "https://s-media-cache-ak0.pinimg.com/originals/0e/59/b3/0e59b3766f8a43fd00912ef0c6d5bb96.jpg",
    "http://www.johnson.cornell.edu/people/faculty/mnc35/mnc35_profile.jpg",
    "http://i.dailymail.co.uk/i/pix/2013/09/02/article-0-1B955792000005DC-63_644x715.jpg"
]
event_count = 1000
user_count = 100
default_password = "password"
user_jwts = []

def random_icon():
    path = os.path.dirname(__file__) + "/icons/*.png"
    files = glob.glob(path)
    file = random.choice(files)
    with open(file, "rb") as image_file:
        return base64.b64encode(image_file.read()).decode()

def random_image():
    path = os.path.dirname(__file__) + "/images/*.png"
    files = glob.glob(path)
    file = random.choice(files)
    with open(file, "rb") as image_file:
        return base64.b64encode(image_file.read()).decode()

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
        "profileImageUrl": random.choice(profile_images)
    }

def random_event_title():
    return "%s %s" % (random.choice(words).capitalize(), random.choice(words))

def create_category_payload(title):
    return {
        "icon": random_icon(),
        "iconContentType": "image/png",
        "theme": random.choice(["RED","ORANGE","YELLOW","GREEN","BLUE","INDIGO","VIOLET"]),
        "title": title
    }

def create_event_payload():
    category = random.choice(event_categories)
    return {
        "title": random_event_title(),
        "description": fake.text(),
        "imageUrl": random.choice(event_images),
        "privateEvent": False,
        "eventCategory": category
    }

def create_location_payload(event, index, lat_tuple, lon_tuple, month, day, start_hour, end_hour):
    address = fake.address()
    return {
        "name" : address,
        "description": fake.text(),
        "address": address,
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

def create_event_attendance(event_id):
    return {
        "eventId": event_id,
        "type": "GOING",
    }

def create_event_image(poll_id):
    return {
        "file": random_image(),
        "pollId": poll_id,
        "fileContentType": "image/png",
    }

def random_user_jwt_selection():
    return random.sample(user_jwts, 10)

def create_jwt_header(token):
    return {
        "Authorization": "Bearer " + token,
        "Content-Type": "application/json"
    }

def main():

    print('# Authenticating user: ' + user_name)
    payload = {"username": user_name, "remember_me": True, "password": user_pass}
    register = requests.post(base_url + "/api/authenticate", json=payload)

    if register.status_code == 401:
        print(register.text)
        exit()

    token = register.json().get("id_token")
    jwt_header = create_jwt_header(token)

    print("# Generating users")
    for u in range(1, user_count):
        generated_user = create_random_user()
        user = requests.post(base_url + "/api/managed-users",
                             headers=jwt_header,
                             json=create_random_user())
        user_login = user.json().get("login")
        user_login_payload = {"username": user_login, "remember_me": True, "password": default_password}
        user_auth = requests.post(base_url + "/api/authenticate", json=user_login_payload)
        user_token = user_auth.json().get("id_token")
        user_jwt_header = create_jwt_header(user_token)
        user_jwts.append(user_jwt_header)


if __name__ == "__main__":
    main()
