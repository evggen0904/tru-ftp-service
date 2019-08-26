#!/usr/bin/env bash

ps ax | grep java | grep tru-ftp-service | awk '{print $1}' | xargs -r kill -9