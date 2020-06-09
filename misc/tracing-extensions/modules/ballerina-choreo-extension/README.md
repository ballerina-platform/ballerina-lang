### Ballerina Choreo Tracing Extension

This extension can be used to connect with Choreo cloud. 

##### Install Guide

- Add following properties to `ballerina.conf` file.
 ```toml
 [b7a.observability.tracing]
 enabled=true
 provider="choreo"
 ```

- Run your Ballerina service with that `ballerina.conf` file.
  - Either place `ballerina.conf` in your applications directory.
  - Or use `--b7a.config.file=path/to/ballerina.conf`

- Once everything is up and running, you can go to Choreo using the URL printed in the Console
