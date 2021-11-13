FROM ubuntu:latest

LABEL maintainer="Mateusz Kubica <mateusz.kubica99@gmail.com>"

RUN apt update && apt upgrade && apt install golang-cfssl openvpn -y

EXPOSE 443

WORKDIR /etc/openvpn/server
COPY ./config .


# CMD chmod +x entrypoint.sh && ./entrypoint.sh

CMD cfssl gencert -remote=ca:8888 -profile=server server-csr.json | cfssljson -bare server && \
	cfssl info -remote=ca:8888 | cfssljson -bare ca && \
	mv server.pem server.crt && \
	mv server-key.pem server.key && \
	mv ca.pem ca.crt && \
	openvpn --genkey --secret ta.key && \
	mkdir /dev/net && mknod /dev/net/tun c 10 200 && \
	openvpn --config server.conf
