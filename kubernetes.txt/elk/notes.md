# Configuring ELK

add the dependency to the pom

```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>6.3</version>
</dependency>
```
add the logstash-spring.xml to src/main/resources (note the logstash appender)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/base.xml"/>
       <springProperty scope="context" name="application_name"
          source="spring.application.name"/>
 
    <appender name="logstash" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>logstash:5000</destination>
       <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>
 
    <root level="INFO">
        <appender-ref ref="logstash"/>
        <appender-ref ref="CONSOLE"/>
    </root>
    <logger name="org.springframework" level="INFO"/>
    <logger name="com.optimagrowth" level="DEBUG"/>
</configuration>
```

add the logstash.conf to your logstash distribution
```yml
input {
  tcp {
    port => 5000
    codec => json_lines
  }
}

filter {
  mutate {
    add_tag => [ "dashboard" ]
  }
}

output {
  elasticsearch {
    hosts => "localhost:9200"
    index => "dashboard-%{+YYYY.MM.dd}"
  }
}
```

edit /etc/kibana/kibana.yml to specify:
 - server.port: 5601
 - elasticsearch.hosts: ["http://127.0.0.1:9200"]
 - server.host: "0.0.0.0"

create an index in Elasticsearch:
 - log in to the elastic/kibana dashboard, click on the menu and choose "analytics/discover"
 - create index pattern "dashboard-*"
 - select the time filter "@timestamp" from the dropdown menu
 - select next and start using Elasticsearch
 - further indexes will be created daily

## Automatically delete old logs
- Go to elasticsearch
- At the far bottom of the menu select "Stack Management"
- Create an "Index Template" on the "Index Management" screen, give it a name, set the index pattern to "dashboard-*" (which means it will apply to all the indices created by logstash since we specified output:elasticsearch:index=>"dashboard-%{+YYYY.MM.dd}" in the logstash config). Go through all the other screens leaving them as default and save.
- Create an "Index Lifecycle Policy" and give it a name, activate the "delete" phase at the bottom of the options and set the number of days. Save your policy.
- Back in the list of index policies, click "actions" next to your policy, choose "Add policy to index template", select your new index template, and click "Add".

Now all new indices created in elasticsearch that start with "dashboard-" will automatically have that template attached to them, and that template has a policy linked to it which will put the index into the "delete" phase after the number of days you specified.

By default, this setup will only put indices into the "delete" phase but not actually delete them! This is a known bug in the current UI (v7.10.1) which may be fixed in future versions.  
For now you need to select your new policy under "Index Lifecycle Policies", select "Show Request" and you'll see something like this. 

```json
PUT _ilm/policy/dashboard_seven_day_delete
{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
          "set_priority": {
            "priority": 100
          }
        }
      },
      "delete": {
        "min_age": "7d",
        "actions": {}
      }
    }
  }
}
```

Copy that to the clipboard, and paste to the Kibana Console which is available in the menu under Management->Dev Tools.
Modify the request to add the "delete" action like this:
```json
PUT _ilm/policy/dashboard_seven_day_delete
{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
          "set_priority": {
            "priority": 100
          }
        }
      },
      "delete": {
        "min_age": "7d",
        "actions": { 
          "delete": {}
        }
      }
    }
  }
}
```
Then use the small green run triangle at the top right of the editor to send the request.  

You now have a delete policy which sets indices into their delete phase after a set number of days, triggering a delete action tied to that phase, and this policy is tied to an index template which is automatically assigned to all new indices created that match the index pattern "dashboard-*".

I honestly don't know how they could have made this any easier.