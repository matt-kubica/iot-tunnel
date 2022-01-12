FROM python:3.9

LABEL maintainer="Mateusz Kubica <mateusz.kubica99@gmail.com>"

RUN apt update && apt upgrade -y && apt install iputils-ping iproute2 dnsutils iptables net-tools sshpass -y

WORKDIR /usr/app
COPY requirements.txt .

RUN pip install --upgrade pip && \
    pip install -r requirements.txt

COPY . .
RUN chmod +x entrypoint.sh
CMD ./entrypoint.sh