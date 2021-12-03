FROM gradle:7.2 AS build

WORKDIR /usr/build
COPY . .

RUN gradle clean build

FROM amazoncorretto:11 AS runtime

WORKDIR /usr/service
COPY --from=build /usr/build/build/libs/management-service-0.0.1-SNAPSHOT.jar app.jar

CMD java -jar app.jar --spring.profiles.active=remote