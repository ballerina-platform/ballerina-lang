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

function main(string[] args) {
    system:println("Hello world");
}
```
- save this file in a directory (let's say under bin directory in the ballerina distribution)

- Go inside the ballerina/bin directory and run the following command
```
./ballerina.sh hello.bal
```

- This will execute the main function and print the "Hello world" in the console.

### Echo service

- create a sample ballerina file (echo.bal) with the following content

```
package samples.echo;
import ballerina.net.http;
import ballerina.lang.message;

service EchoService{

  @POST
  @Path ("/*")
  resource echoResource (message m) {
    http:convertToResponse(m);
    reply m;
  }

}
```
- save this file in a directory (let's say under bin directory in the ballerina distribution)

- Go inside the ballerina/bin directory and run the following command
```
./ballerinaserver.sh echo.bal
```

- This will start the ballerina server with the port 9090 and you can invoke the service with the following curl command.
```
curl -v -d "<Hello>World</Hello>" http://localhost:9090/EchoService
```
Above command will echo back the same message and you can see the result like below
```
<Hello>World</Hello>
```

### Message Passthrough service

- create a sample ballerina file (passthrough.bal) with the following content

```
package samples.message.passthrough;

import ballerina.lang.message;
import ballerina.net.http as http;


@BasePath ("/passthrough")
service PassthroughService {

    @POST
    @Path ("/stocks")
    resource passthrough (message m) {
        http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:9090", 100);
        message response;
        response = http:HTTPConnector.post(nyseEP, "/NYSEStocks", m);
        reply response;
    }
}

@BasePath("/NYSEStocks")
service NYSEStockQuote {
  @POST
  @Path("/*")
  resource stocks (message m) {
    message response;
    json payload;
    response = new message;
    payload = `{"exchange":"nyse", "name":"IBM", "value":"127.50"}`;
    message:setJsonPayload(response, payload);
    reply response;
  }
}

```
- save this file in a directory (let's say under bin directory in the ballerina distribution)

- Go inside the ballerina/bin directory and run the following command
```
./ballerinaserver.sh passthrough.bal
```

- This will start the ballerina server with the port 9090 and you can invoke the service with the following curl command.
```
curl -v -d "{"name":"NYSE"}" http://localhost:9090/passthrough/stocks
```
Above command will return the stock details from the NYSE sample service written above
```
{"exchange":"nyse","name":"IBM","value":"127.50"}
```

### Content Based Routing service

- create a sample ballerina file (content-based-router.bal) with the following content

```
package samples.contentaware;

import ballerina.net.http;
import ballerina.lang.json;
import ballerina.lang.message;
import ballerina.lang.system;

@BasePath ("/stock")
service ContentBasedRouteService {

  @POST
  @Path("/*")
  resource cbrResource (message m) {
    // Connector declarations
    http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:9090/NYSEStocks", 30000);
    http:HTTPConnector nasdaqEP = new http:HTTPConnector("http://localhost:9090/NASDAQStocks", 60000);

    // Variable declarations
    message response;
    json jsonMsg;
    json errorMsg;
    string result;
    string nameString;
    string nyseString;
    message request;
    json requestJson;

    // Assignment statements
    nyseString = "NYSE";

    // Extract the JSON message
    jsonMsg = message:getJsonPayload(m);

    // Convert to string
    result = json:toString(jsonMsg);
    system:println(result);

    // Evaluate JSON path and get the result
    nameString = json:getString(jsonMsg, "$.name");
    system:println(nameString);

    // Set the extracted message to outgoing message
    requestJson = json:getJson(jsonMsg, "$");
    message:setJsonPayload(m, requestJson);

    if (nameString == nyseString) {
        response = http:HTTPConnector.post(nyseEP, "/", m);
    } else {
        response = http:HTTPConnector.post(nasdaqEP, "/", m);
    }
    reply response;
  }
}

@BasePath("/NYSEStocks")
service NYSEStockQuote {

  @POST
  @Path("/*")
  resource stocks (message m) {
    message response;
    json payload;
    response = new message;
    payload = `{"exchange":"nyse", "name":"IBM", "value":"127.50"}`;
    message:setJsonPayload(response, payload);
    reply response;
  }
}

@BasePath("/NASDAQStocks")
service NASDAQStockQuote {

  @POST
  @Path("/*")
  resource stocks (message m) {
    message response;
    json payload;
    response = new message;
    payload = `{"exchange":"nasdaq", "name":"IBM", "value":"999.50"}`;
    message:setJsonPayload(response, payload);
    reply response;
  }
}
```
- save this file in a directory (let's say under bin directory in the ballerina distribution)

- Go inside the ballerina/bin directory and run the following command
```
./ballerinaserver.sh content-based-router.bal
```

- This will start the ballerina server with the port 9090 and you can invoke the service with the following curl command.
```
curl -v -d "{"name":"NYSE"}" http://localhost:9090/stock
```
Above command will return the stock details from the NYSE sample service written above
```
{"exchange":"nyse","name":"IBM","value":"127.50"}
```
Run the following command to execute the nasdaq service
```
curl -v -d "{"name":"NASDAQ"}" http://localhost:9090/stock
```
Above command will return the stock details from the NYSE sample service written above
```
{"exchange":"nasdaq","name":"IBM","value":"999.50"}
```