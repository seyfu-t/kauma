FROM ghcr.io/johndoe31415/labwork-docker:master

COPY . /kauma

WORKDIR /kauma

RUN ./build

CMD [ "./kauma", "./app/src/test/resources/AllInput.json" ]