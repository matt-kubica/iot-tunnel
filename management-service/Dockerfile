FROM amazoncorretto:11

#WORKDIR /usr/service
#COPY gradlew settings.gradle build.gradle gradle/* ./
#RUN ./gradlew tasks

COPY . .
RUN ./gradlew clean build

CMD java -jar build/libs/management-service-0.0.1-SNAPSHOT.jar