ARG MSVC_NAME=msvc-gateway

FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu as builder

ARG MSVC_NAME

WORKDIR /app/$MSVC_NAME

COPY ./pom.xml /app
COPY ./$MSVC_NAME/.mvn ./.mvn
COPY ./$MSVC_NAME/mvnw .
COPY ./$MSVC_NAME/pom.xml .

RUN ./mvnw clean package -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip && rm -r ./target/
COPY ./$MSVC_NAME/src ./src
RUN ./mvnw clean package -DskipTests

FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

ARG MSVC_NAME

WORKDIR /app
RUN mkdir ./logs
COPY --from=builder /app/$MSVC_NAME/target /app/target
ARG PORT_APP=8090
ENV PORT $PORT_APP
EXPOSE $PORT

CMD [ "java", "-jar", "./target/msvc-gateway-0.0.1-SNAPSHOT.jar" ]