FROM ubuntu:latest

LABEL maintainer="Mateusz Kubica <mateusz.kubica99@gmail.com>"

RUN apt update && apt upgrade && apt install golang-cfssl -y

EXPOSE 8888
VOLUME /etc/common/
VOLUME /etc/cfssl/ca/pki

WORKDIR /etc/cfssl/ca
COPY ./config .

RUN chmod +x entrypoint.sh
CMD ./entrypoint.sh


