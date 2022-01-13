FROM python:3.9

LABEL maintainer="Mateusz Kubica <mateusz.kubica99@gmail.com>"

WORKDIR /usr/app
COPY requirements.txt .

RUN pip install --upgrade pip && \
    pip install -r requirements.txt

COPY . .
ENTRYPOINT python src/app.py
