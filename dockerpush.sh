#!/bin/bash

VERSION=$(./gradlew showVersion | sed '2q;d')

./gradlew bootRepackage -Pprod buildDocker
docker tag backend:latest plastboks/plod:$VERSION
docker push plastboks/plod:$VERSION
