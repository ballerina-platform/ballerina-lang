## Writing integration tests for Ballerina

Currently both Java and JMeter based integration tests are supported. However `default` Maven profile will only run Java based integration tests, while `integration` Maven profile will run both the JMeter and Java based tests. Java based tests in the `default` profile will be run against a Ballerina server spawned as a forked process. This is done differently in `integration` profile where the Ballerina server is spawned as a Docker container. The `integration` profile is targetted to be run with a less frequency than the `default` profile, and will require Docker to be in the build host machine.

> Make sure `docker` can be accessed without using `sudo`. If not follow the steps mentioned in the [Post-installation steps for Linux](https://docs.docker.com/engine/installation/linux/linux-postinstall/) documentation.

### Java based tests

Java based tests can be found `src/test/java/` folder. The existing ones make calls to the Ballerina sample Services to test Ballerina. To only run the Java based tests, simply invoke the following goals. 

```bash
mvn clean install
```

### JMeter based tests

JMeter tests can be added to verify scenarios in Ballerina that are not covered by the samples. To run JMeter tests along with the Java tests against a Ballerina Container, invoke the following goals with `integration` build profile.

```bash
mvn clean install -P integration
```

#### Adding a new JMeter based test
##### Ballerina Services
Write sample Ballerina services and copy the `.bal` files to a location inside `src/test/resources/ballerina/`. These services should not host endpoints that are in conflict with the samples that are shipped with the Ballerina distribution.

##### JMeter Scripts
Create the related JMeter scripts and copy the resulting `.jmx` file to a location inside `src/test/jmeter/`. When creating the JMeter scripts, parameterize the hostname to have `localhost` as the value, and the port to retrieve the value from `ballerina_port` JMeter Property. This value will be populated by the spawned Docker Container, as a dynamic port has been mapped to the Container's internal port `9090`.

```
host: localhost
port: ${__P(ballerina_port)}
```

> Make sure that the JMeter tests have proper Assertion Listeners to generate assertion failures in case the tests fail. The Maven run will not fail on test failure, unless proper assertions are present in the JMeter scripts.