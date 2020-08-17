FROM openjdk:12.0.2

EXPOSE 8082

ADD ./target/recommendation-service-0.0.1-SNAPSHOT.jar recommendation-service.jar

RUN mkdir /etc/me
WORKDIR /etc/me/
VOLUME [ "/etc/me/" ]

ENTRYPOINT ["java","-jar","/recommendation-service.jar"]