FROM python:3.9

LABEL maintainer="Mateusz Kubica <mateusz.kubica99@gmail.com>"

RUN apt update && apt upgrade -y && apt install curl openvpn netcat -y

WORKDIR /usr/app
COPY . .

RUN chmod +x entrypoint.sh && \
    pip install -r requirements.txt
CMD ./entrypoint.sh
