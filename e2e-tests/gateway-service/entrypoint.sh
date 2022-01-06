#!/bin/bash

set -m
HOSTNAME=$(hostname)
URL_BASE=http://"$MANAGEMENT_SERVICE_ADDRESS":"$MANAGEMENT_SERVICE_PORT"

echo "Waiting for system to start..."
until curl --output /dev/null --silent --fail "$URL_BASE"/actuator/health; do
    sleep 10
done
echo "System accessible!"

curl -X POST \
     -H "Content-Type: application/json" \
     -d "{\"commonName\": \"$HOSTNAME\"}" \
     "$URL_BASE"/gateway

curl "$URL_BASE"/gateway-config/"$HOSTNAME" > /tmp/client.conf

[ ! -f /dev/net/tun ] && mkdir /dev/net && mknod /dev/net/tun c 10 200

openvpn --config /tmp/client.conf &
python src/app.py


