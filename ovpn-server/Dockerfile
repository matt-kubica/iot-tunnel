FROM ubuntu:latest

LABEL maintainer="Mateusz Kubica <mateusz.kubica99@gmail.com>"

RUN apt update && apt upgrade -y && apt install golang-cfssl openvpn curl iptables gettext-base -y

EXPOSE 443
VOLUME /etc/common
VOLUME /etc/openvpn/server/pki
VOLUME /var/log

WORKDIR /etc/openvpn/server
COPY ./config .

RUN chmod +x entrypoint.sh
CMD ./entrypoint.sh
