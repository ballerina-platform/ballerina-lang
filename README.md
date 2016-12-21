# Hello, I'm Ballerina.

Ballerina is a new programming language for integration built on a sequence diagram metaphor. Ballerina is:
- Simple
- Intuitive
- Visual
- Powerful
- Lightweight
- Cloud Native
- Container Native
- Fun

The conceptual model of Ballerina is that of a sequence diagram. Each participant in the integration gets its own lifeline and Ballerina defines a complete syntax and semantics for how the sequence diagram works and execute the desired integration.

Ballerina is not designed to be a general purpose language. Instead you should use Ballerina if you need to integrate a collection of network connected systems such as HTTP endpoints, Web APIs, JMS services, and databases. The result of the integration can either be just that - the integration that runs once or repeatedly on a schedule, or a reusable HTTP service that others can run.

## Documents
[Informal Introduction to Ballerina](docs/SyntaxSummary.md)

# Hello world with Ballerina

## Build from the source

- Get a clone or download source from [github](https://github.com/wso2/ballerina)
- Run maven build from the root directory
- Extract the ballerina distribution created at ballerina/modules/distribution/target/ballerina-1.0.0-SNAPSHOT.zip in to your local directory

## Running samples

### Hello function

- create a sample ballerina file (hello.bal) with the following content

```
package samples.hello;
import ballerina.lang.system;

function main(int z) {
    system:println("Hello world");
}
```
- save this file in a directory (let's say under bin directory in the ballerina distribution)

- Go inside the ballerina/bin directory and run the following command
```
./ballerina.sh run hello.bal
```

- This will execute the main function and print the "Hello world" in the console.

### Echo service

- create a sample ballerina file (echo.bal) with the following content

```
package samples.echo;

service EchoService{

  @POST
  @Path ("/*")
  resource echoResource (message m) {
      reply m;
  }

}
```
- save this file in a directory (let's say under bin directory in the ballerina distribution)

- Go inside the ballerina/bin directory and run the following command
```
./ballerina.sh run echo.bal
```

- This will start the ballerina server with the port 9090 and you can invoke the service with the following curl command.
```
curl -v -d "<Hello>World</Hello>" http://localhost:9090/EchoService
```
Above command will echo back the same message and you can see the result like below
```
<Hello>World</Hello>
```


