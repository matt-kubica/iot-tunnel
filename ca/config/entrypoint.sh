#!/bin/bash

# current dir `/etc/cfssl/ca`
# dir for pki `/etc/cfssl/ca/pki`
# common dir `/etc/common`

echo "Waiting for database to start..."
while ! nc -z database 5432; do
  sleep 1
done
echo "Database accessible!"

if [[ ! -f /etc/common/ca.crt ]] || [[ ! -f /etc/cfssl/ca/pki/ca.key ]]
then
	echo "Missing ca.crt or ca.key, generating new ones..."
	cfssl gencert -initca ca-csr.json | cfssljson -bare ca - && \
	mv ca.pem /etc/common/ca.crt && \
	mv ca-key.pem /etc/cfssl/ca/pki/ca.key
else
  echo "Reusing ca.cert and ca.key found on volume..."
fi

cat db-config.json | envsubst > substituted-db-config.json

cfssl serve \
	-address=0.0.0.0 \
	-port=8888 \
	-ca=/etc/common/ca.crt \
	-ca-key=/etc/cfssl/ca/pki/ca.key \
	-config=ca-config.json \
	-db-config=substituted-db-config.json
