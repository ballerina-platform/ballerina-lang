### Ballerina Zipkin Extension

##### Install Guide

- Start Zipkin. You can use their docker image using following command. `docker run -d -p 9411:9411 openzipkin/zipkin`.
- Build `ballerina-zipkin-extension` and put it in `bre/lib/` directory.
- Create a `trace-config.yaml` with following properties.
```yaml
tracers:
  - name: zipkin
    enabled: true
    className: org.ballerinalang.observe.trace.extension.zipkin.ZipkinTracerProvider
    configuration:
      sampler.type: const
      sampler.param: 1
      reporter.log.spans: true
      reporter.hostname: localhost
      reporter.port: 9411
      reporter.flush.interval.ms: 1000
      reporter.max.buffer.spans: 1000
```
- Create a `ballerina.conf` file with `trace.config` property, which points to the above `trace-config.yaml`.
- Run your Ballerina service with that `ballerina.conf` file.
  - Either place `ballerina.conf` in your applications directory.
  - Or use `-Bballerina.conf=path/to/ballerina.conf`
- Once everything is up and running, you can use Zipkin dashboard to view traces.
