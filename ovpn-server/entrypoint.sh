#!/bin/bash

echo "Generating certificate for openvpn server with remote CA..."
cfssl gencert -remote=ca:8888 -profile=server server-csr.json | cfssljson -bare server
cfssl info -remote=ca:8888 | cfssljson -bare ca

echo "Changing default names of generated files..." 
mv server.pem server.crt && mv server-key.pem server.key && mv ca.pem ca.crt

echo "Generating TLS key..." 
openvpn --genkey --secret ta.key 

echo "Launching OpenVPN server..." 
openvpn --config server.conf

ping ca