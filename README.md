# Fog Computing

This multi-module project is split up into 4 parts:
- `sensors` - To small python services producing data via a ZeroMQ PUB sockets. They can be started via `docker-compose` 
and are exposed on ports `5555` (usage topic) and `5556` (temperature topic).
- `edge` - a small service collecting data from 2 sensors via subscribing to a ZeroMQ PUB socket and sending over data 
to the cloud service via a ZeroMQ REQ socket. Data is collected every second and send over to the cloud server every 15 
seconds.
- `cloud` - a small server waiting for messages of the edge service via a ZeroMQ REP socket which answers with 
aggregated sensor data.
- `common` for the shared code parts of the 2 other modules.

# How to use

The [EdgeService](edge/src/main/java/com/fogcomputing/EdgeService.java) expects the cloud service to run on
`localhost:8080` per default. This can be overwritten by passing an address of format `host:port` as first argument 
to the edge service. 

The [CloudService](cloud/src/main/java/com/fogcomputing/CloudService.java) runs on port `8080` per default. This can be 
overwritten by passing a port option to the cloud service via `-p` or `--port`.

Cloud and edge service are resilient to outages of the respective other service and can be started in any order. The 
edge service will collect sensor data no matter if the cloud service is reachable. Usually, the edge service sends 
a [SensorDataBatch](common/src/main/java/com/fogcomputing/SensorDataBatch.java) every 15 seconds containing 15
[SensorData](common/src/main/java/com/fogcomputing/SensorData.java) data points. If the cloud service is not reachable,
the amount of messages per batch will grow (up to infinity) until the cloud service is reachable again.
