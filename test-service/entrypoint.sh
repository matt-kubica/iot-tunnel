#!/bin/bash

OVPN_IP=$(dig +short iot-tunnel-ovpn-server)

ip route add 10.8.0.0/24 via $OVPN_IP dev eth0

sleep infinity
