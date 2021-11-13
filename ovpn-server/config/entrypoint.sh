#!/bin/bash

# generate certificate for openvpn server with remote CA
cfssl gencert -remote=ca:8888 -profile=server server-csr.json | cfssljson -bare server

# change default names of generated files
mv server.pem server.crt && mv server-key.pem server.key 

# generate TLS key
openvpn --genkey --secret ta.key

# launch openvpn server
openvpn --config server.conf