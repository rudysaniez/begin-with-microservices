FROM openjdk:12.0.2

EXPOSE 8081

ADD ./target/product-service-0.0.1-SNAPSHOT.jar product-service.jar

RUN mkdir /etc/me
WORKDIR /etc/me/
VOLUME [ "/etc/me/" ]

ENTRYPOINT ["java","-jar","/product-service.jar"]