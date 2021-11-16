#!/bin/bash

cfssl gencert -remote=ca:8888 -profile=client client-csr.json | cfssljson -bare client

cp /etc/common/ca.crt .
cp /etc/common/ta.key .
mv client.pem client.crt
mv client-key.pem client.key

cat base.conf \
	<(echo -e "\nremote $OVPN_SERVER_IP 443\n") \
    <(echo -e "<ca>") \
    ./ca.crt \
    <(echo -e "</ca>\n<cert>") \
    ./client.crt \
    <(echo -e "</cert>\n<key>") \
    ./client.key \
    <(echo -e "</key>\n<tls-crypt>") \
    ./ta.key \
    <(echo -e "</tls-crypt>") > /etc/common/client.conf
