### Ballerina Jaeger Extension

##### Install Guide

- Start Jaeger. You can use their docker image using following command. `docker run -d -p 13133:13133 -p 16686:16686 
-p 4317:4317 jaegertracing/all-in-one`.
- Build `ballerina-jaeger-extension` and put it in `bre/lib/` directory.
- Create a `trace-config.yaml` with following properties.
```yaml
tracers:
  - name: jaeger
    enabled: true
    className: org.ballerinalang.observe.trace.extension.jaeger.JaegerTracerProvider
    configuration:
      sampler.type: const
      sampler.param: 1
      reporter.log.spans: true
      reporter.hostname: localhost
      reporter.port: 4317
      reporter.flush.interval.ms: 1000
      reporter.max.buffer.spans: 1000
```
- Create a `ballerina.conf` file with `trace.config` property, which points to the above `trace-config.yaml`.
- Run your Ballerina service with that `ballerina.conf` file.
  - Either place `ballerina.conf` in your applications directory.
  - Or use `-Bballerina.conf=path/to/ballerina.conf`
- Once everything is up and running, you can use Jaeger dashboard to view traces.
