ARG MSVC_NAME=msvc-usuarios

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
ARG PORT_APP=8001
ENV PORT $PORT_APP
EXPOSE $PORT

# ENTRYPOINT [ "java", "-jar", "./target/msvc-usuarios-0.0.1-SNAPSHOT.jar" ]
CMD [ "java", "-jar", "./target/msvc-usuarios-0.0.1-SNAPSHOT.jar" ]