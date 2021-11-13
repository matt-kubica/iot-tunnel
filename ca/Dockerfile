FROM ubuntu:latest

LABEL maintainer="Mateusz Kubica <mateusz.kubica99@gmail.com>"

RUN apt update && apt upgrade && apt install golang-cfssl -y

EXPOSE 8888

WORKDIR /cfssl/ca
COPY config .
RUN cfssl gencert -initca ca-csr.json | cfssljson -bare ca -  

CMD cfssl serve \
	-address=0.0.0.0 \
	-port=8888 \
	-ca=ca.pem \
	-ca-key=ca-key.pem \
	-config=ca-config.json


