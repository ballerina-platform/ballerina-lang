### Ballerina Jaeger Extension

##### Install Guide

- Start Jaeger. You can use their docker image using following command. `docker run -d -p5775:5775/udp 
-p6831:6831/udp -p6832:6832/udp -p5778:5778 -p16686:16686 -p14268:14268 
jaegertracing/all-in-one:latest`.
- Build `ballerina-jaeger-extension` and put it in `bre/lib/` directory.
- Download following jar files and place them in `bre/lib/` directory.
  - [jaeger-core-0.24.0.jar] [1]
  - [jaeger-thrift-0.24.0.jar] [2]
  - [libthrift-0.11.0.jar] [3]
  - [okhttp-3.9.1.jar] [4]
  - [okio-1.13.0.jar] [5]
- Create a `trace-config.yaml` with following properties.
```yaml
tracers:
  - name: jaeger
    enabled: true
    className: org.ballerinalang.observe.trace.extension.jaeger.OpenTracingExtension
    configuration:
      sampler.type: const
      sampler.param: 1
      reporter.log.spans: true
      reporter.hostname: localhost
      reporter.port: 5775
      reporter.flush.interval.ms: 1000
      reporter.max.buffer.spans: 1000
```
- Create a `ballerina.conf` file with `trace.config` property, which points to the above `trace-config.yaml`.
- Run your Ballerina service with that `ballerina.conf` file.
  - Either place `ballerina.conf` in your applications directory.
  - Or use `-Bballerina.conf=path/to/ballerina.conf`
- Once everything is up and running, you can use jaeger dashboard to view traces.

[1]: http://central.maven.org/maven2/com/uber/jaeger/jaeger-core/0.24.0/jaeger-core-0.24.0.jar
[2]: http://central.maven.org/maven2/com/uber/jaeger/jaeger-thrift/0.24.0/jaeger-thrift-0.24.0.jar
[3]: http://central.maven.org/maven2/org/apache/thrift/libthrift/0.11.0/libthrift-0.11.0.jar
[4]: http://central.maven.org/maven2/com/squareup/okhttp3/okhttp/3.9.1/okhttp-3.9.1.jar
[5]: http://central.maven.org/maven2/com/squareup/okio/okio/1.13.0/okio-1.13.0.jar