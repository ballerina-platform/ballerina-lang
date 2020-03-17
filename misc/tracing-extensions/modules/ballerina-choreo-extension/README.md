### Ballerina Choreo Tracing Extension

This extension can be used to connect with Choreo cloud. 

##### Install Guide

- Add following properties to `ballerina.conf` file.
 ```toml
 [b7a.observability.tracing]
 enabled=true
 name="choreo"
 ```

- Run your Ballerina service with that `ballerina.conf` file.
  - Either place `ballerina.conf` in your applications directory.
  - Or use `--b7a.config.file=path/to/ballerina.conf`

- Once everything is up and running, you can use Choreo dashboard to view traces.
