FROM openjdk:12.0.2

EXPOSE 8083

ADD ./target/review-service-0.0.1-SNAPSHOT.jar review-service.jar

RUN mkdir /etc/me
WORKDIR /etc/me/
VOLUME [ "/etc/me/" ]

ENTRYPOINT ["java","-jar","/review-service.jar"]