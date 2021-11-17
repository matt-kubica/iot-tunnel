FROM ubuntu:latest

LABEL maintainer="Mateusz Kubica <mateusz.kubica99@gmail.com>"

RUN apt update && apt upgrade && apt install golang-cfssl curl -y

VOLUME /etc/common/

WORKDIR /tmp
COPY ./config .

RUN chmod +x entrypoint.sh
CMD ./entrypoint.sh
