FROM openjdk:slim
MAINTAINER Chahrazed LABBA (chahrazedlabba@gmail.com)
RUN apt-get update
WORKDIR /Appagent
COPY target/bpms-V1multitenantsim-0.0.1-SNAPSHOT.jar /Appagent/bpms.jar
COPY lib/jade.jar /Appagent/jade.jar
COPY lib/dummy.jar /Appagent/dummy.jar
COPY dummyscript.sh /Appagent/dummyscript.sh
ARG BPMSNAME
ARG URL
ARG CONFIGFILE
ENV BPMSNAME=${BPMSNAME}
ENV URL=${URL}
ENV CONFIGFILE=${CONFIGFILE}
CMD java -jar bpms.jar ${BPMSNAME} ${URL} ${CONFIGFILE}
