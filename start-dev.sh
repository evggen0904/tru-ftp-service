#!/usr/bin/env bash
JAR_FILE=tru-ftp-service.jar
echo "JAR_FILE ${JAR_FILE}"
ps ax | grep java | grep tru-ftp-service | awk '{print $1}' | xargs -r kill -9
java -jar -Dspring.profiles.active=demo1 ${JAR_FILE} $@ > /dev/null &