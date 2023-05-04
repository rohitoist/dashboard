#!/bin/bash

java -jar -Dserver.port=80 -Dspring.mail.host=10.97.211.110 -Dspring.profiles.active=dmz /opt/emailbroker/email-broker-0.0.1-SNAPSHOT.jar &