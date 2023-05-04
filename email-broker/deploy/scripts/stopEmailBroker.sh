!/bin/bash

EMAIL=email-broker-0.0.1-SNAPSHOT.jar

# emailpid=$(jps -l | grep $EMAIL | awk '{print $1}')

emailpid=$(ps -ef | grep $EMAIL | awk '{print $2}')
[ ! -z "$emailpid" ] && kill $emailpid