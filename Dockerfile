FROM ubuntu:16.04

# Install Java, ant and unzip
RUN apt-get update -y && apt-get install -y default-jdk ant unzip

WORKDIR /opt/odv

COPY src src/
COPY lib lib/
COPY nbproject nbproject/
COPY build.xml build.xml

# Compile, extract libraries, deploy
RUN ant compile && \
    unzip -o lib/gson/gson-2.8.5.jar -d build/classes/ && \
    unzip -o lib/javacsv2.1/javacsv.jar -d build/classes/ && \
    unzip -o lib/java-runtime-compiler/compiler-2.3.1.jar -d build/classes/ && \
    unzip -o lib/picocli/picocli-4.0.0-alpha-2.jar -d build/classes/ && \
    ant jar
