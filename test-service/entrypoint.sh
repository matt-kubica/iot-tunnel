#!/bin/bash

OVPN_SERVER_INTERNAL_IP=$(dig +short iot-tunnel-ovpn-server)

ip route add "$OVPN_INTERNAL_NETWORK_ADDRESS"/"$OVPN_INTERNAL_NETWORK_MASK" via "$OVPN_SERVER_INTERNAL_IP" dev eth0

sleep infinity
