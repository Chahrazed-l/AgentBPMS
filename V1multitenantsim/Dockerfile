FROM openjdk:slim
MAINTAINER Chahrazed LABBA (chahrazedlabba@gmail.com)
RUN apt-get update
COPY target/bpms-V1multitenantsim-0.0.1-SNAPSHOT.jar bpms.jar
COPY config.txt config.txt
ARG BPMSNAME
ARG URL
ARG CONFIGFILE
ARG ACTIVETASK
ENV BPMSNAME=${BPMSNAME}
ENV URL=${URL}
ENV CONFIGFILE=${CONFIGFILE}
ENV ACTIVETASK=${ACTIVETASK}
CMD java -jar bpms.jar ${BPMSNAME} ${URL} ${CONFIGFILE} ${ACTIVETASK}
