# iot-tunnel

In order to test infrastructure provisioning simply type `docker compose up` on a remote machine with docker runtime installed, register gateway (open-vpn client) by sending POST request to `http://<remote-ip>:8080/gateway` with body containing mandatory key `commonName`, then obtain config by sending GET request to `/gateway-config/<common-name>`. Upload config to openvpn client and try to ping ovpn server or other service from docker compose.

This config won't route and NAT all outbound traffic via OpenVPN server to the internet because it is not a purpose of this setup.
