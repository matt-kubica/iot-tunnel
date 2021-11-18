# iot-tunnel

In order to test infrastructure provisioning simply type `docker compose up` on a remote machine with docker runtime installed, obtain client's config by typing `docker exec -it iot-tunnel-ovpn-server bash -c 'cat /etc/common/client.conf'`, upload config to OpenVPN client and try to connect to server, or any other service.

This config won't route and NAT all outbound traffic via OpenVPN server to the internet because it is not a purpose of this setup.
