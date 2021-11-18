FROM ubuntu:latest

LABEL maintainer="Mateusz Kubica <mateusz.kubica99@gmail.com>"

RUN apt update && apt upgrade && apt install iputils-ping iproute2 dnsutils iptables -y

VOLUME /etc/common
VOLUME /var/log

WORKDIR /usr/app
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh
CMD ./entrypoint.sh
