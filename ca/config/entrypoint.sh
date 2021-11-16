#!/bin/bash

# current dir `/etc/cfssl/ca`
# dir for pki `/etc/cfssl/ca/pki`
# common dir `/etc/common`

if [[ ! -f /etc/common/ca.crt ]] || [[ ! -f /etc/cfssl/ca/pki/ca.key ]] 
then 
	echo "Missing ca.crt or ca.key, generating new ones..."
	cfssl gencert -initca ca-csr.json | cfssljson -bare ca - && \
	mv ca.pem /etc/common/ca.crt && \
	mv ca-key.pem /etc/cfssl/ca/pki/ca.key
fi

cfssl serve \
	-address=0.0.0.0 \
	-port=8888 \
	-ca=/etc/common/ca.crt \
	-ca-key=/etc/cfssl/ca/pki/ca.key \
	-config=ca-config.json