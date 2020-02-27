# Spring Boot + Micrometer + InfluxDB

## Inspiration
* https://hub.docker.com/_/influxdb
* https://medium.com/@rohansaraf/monitoring-in-springboot-2-0-micrometer-influxdb-chronograf-d049698bfa33
* https://egkatzioura.com/2020/02/24/spring-boot-and-micrometer-with-inlfuxdb-part-1-the-base-project/

## Instructions
### Run InfluxDB server
```docker run --name=influxdb -d -p 8086:8086 influxdb```

### Run Application
```gradle bootrun```

#### Make requests
curl -i http://localhost:8080/foo

### InfluxDB client
Run the client: ```docker exec -it influxdb influx```
```
use springmicrometerandinfluxdb
select * from "timed"
```