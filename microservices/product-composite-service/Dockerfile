FROM openjdk:12.0.2

EXPOSE 9080

ADD ./target/product-composite-service-0.0.1-SNAPSHOT.jar product-composite-service.jar

RUN mkdir /etc/me
WORKDIR /etc/me/
VOLUME [ "/etc/me/" ]

ENTRYPOINT ["java","-jar","/product-composite-service.jar"]