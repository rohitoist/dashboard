# Install ELK

## Installing
Download the RPMs for each of elasticsearch, kibana, and logstash.
Then install and enable them, e.g.:
```
rpm -ivh kibana-7.10.1-x86_64.rpm
systemctl daemon-reload
systemctl enable kibana.service
```

## Essential setup
### Elasticsearch
Elasticsearch extracts a JNA package into a tmp directory, and as a quirk of the way it runs it must have execute permissions on that JNA package. By default this is /tmp. On hardened linux systems /tmp is mounted with noexec so either:

change the tmp directory to some other volume with mounted with exec in /etc/elasticsearch/jvm.options by adding this line
```
-Djava.io.tmpdir=/some/where/with/exec/privs
```
OR mount /tmp with exec (NOT noexec)
```
mount -o remount,exec /tmp
```


## Start 
As usual for each service. E.g.
```
systemctl start kibana.service
```

### Check they work
#### Elasticsearch
The following should respond with some json describing the elasticsearch application
```
curl localhost:9200
```
#### 