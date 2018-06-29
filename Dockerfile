FROM openjdk:slim
MAINTAINER Chahrazed LABBA (chahrazedlabba@gmail.com)
RUN apt-get update
WORKDIR /Appagent
COPY target/bpms-V1multitenantsim-0.0.1-SNAPSHOT.jar /Appagent/bpms.jar
COPY dummyscript.sh /Appagent/dummyscript.sh
COPY target/classes /Appagent/classes 
COPY target/lib/jade.jar /Appagent/jade.jar
ARG BPMSNAME
ARG URL
ARG CONFIGFILE
ARG TIMEATTENTE
ENV BPMSNAME=${BPMSNAME}
ENV URL=${URL}
ENV CONFIGFILE=${CONFIGFILE}
ENV TIMEATTENTE=${TIMEATTENTE}
CMD java -jar bpms.jar ${BPMSNAME} ${URL} ${CONFIGFILE} ${TIMEATTENTE} 
