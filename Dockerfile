ARG INPUT_JSON=./app/src/test/resources/input/AllInput.json

FROM ghcr.io/johndoe31415/labwork-docker:master

COPY . /kauma

WORKDIR /kauma

RUN ./build

CMD ./kauma $INPUT_JSON
