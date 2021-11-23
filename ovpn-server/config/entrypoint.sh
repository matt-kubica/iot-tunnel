#!/bin/bash

# current dir `/etc/openvpn/server`
# dir for pki `/etc/openvpn/server/pki`
# common dir `/etc/common`

echo "Waiting for CA to start..."
until curl --output /dev/null --silent --fail http://ca:8888/api/v1/cfssl/scaninfo; do
    sleep 1
done
echo "CA accessible!"

if [[ ! -f /etc/openvpn/server/pki/server.key ]] || [[ ! -f /etc/common/server.crt ]]
then
	echo "Missing server.crt or server.key, generating new ones..."
	cfssl gencert -remote=ca:8888 -profile=server server-csr.json | cfssljson -bare server && \
	mv server.pem /etc/common/server.crt && \
	mv server-key.pem /etc/openvpn/server/pki/server.key
else
  echo "Reusing server.crt and server.key found on volume..."
fi

if [[ ! -f /etc/common/ta.key ]]
then
	echo "Missing ta.key, generating new one..."
	openvpn --genkey --secret ta.key && \
	mv ta.key /etc/common/ta.key
else
  echo "Reusing ta.key found on volume..."
fi;


[ ! -f /dev/net/tun ] && mkdir /dev/net && mknod /dev/net/tun c 10 200

echo "Launching OpenVPN server..."
openvpn --config server.conf
