#!/bin/bash

OVPN_SERVER_INTERNAL_IP=$(dig +short iot-tunnel-ovpn-server)
URL_BASE=http://"$MANAGEMENT_SERVICE_ADDRESS":"$MANAGEMENT_SERVICE_PORT"

ip route add "$OVPN_INTERNAL_NETWORK_ADDRESS"/"$OVPN_INTERNAL_NETWORK_MASK" via "$OVPN_SERVER_INTERNAL_IP" dev eth0

echo "Waiting for system to start..."
until curl --output /dev/null --silent --fail "$URL_BASE"/actuator/health; do
    sleep 10
done
echo "System accessible!"

python src/app.py
