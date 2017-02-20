#HTTP/2 Server Connector

##About HTTP/2

The Hypertext Transfer Protocol (HTTP) is a wildly successful protocol,  but still has some weaknesses with HTTP 1.0 and 1.1 versions.

HTTP/2 was introduced to address those drawbacks with features of Binary information exchange, compressed headers, multiplexing, and Server Push.

##HTTP/2 Server Connector

The HTTP/2 server connector allows Ballerina programmers to expose their APIs to HTTP/2 clients. So when you send an HTTP request using an HTTP/2 client, the server will upgrade the protocol to HTTP/2 and send the response as below.

```
HTTP/1.1 101 Switching Protocols
Connection: upgrade
Upgrade:h2c
HTTP/2.0 200
Http-to-http2-upgrade true
```

Then server will send a response to the user request through the HTTP/2 protocol.

##How to define an HTTP/2 service

You can design the service the same as an HTTP service and deploy it in the Ballerina server. The service will be served with both HTTP/1 and HTTP/2 clients after enabling HTTP/2 Support.

```
HTTP/1 > Ballerina > response with HTTP/1
HTTP/2 > Ballerina > response with HTTP/2
HTTPS  > Ballerina >  response with HTTP/1
HTTPS/2 > Ballerina >  response with HTTP/2
```

### HTTP/2 Multiplexing
HTTP/2 Multiplexing allows users to send multiple requests with the same TCP connection using multiple streams. One stream will have the request-response model.

Ballerina provides an example [HTTP/2 Java client][3] and [test samples][4]. The test code uses the same HTTPClient 
connection to send multiple requests.

## Enable the HTTP/2 transport

HTTP/2 is disabled by default. Follow these steps to initiate HTTP/2.

### Configure the listener

Open the `$BALLERINA_HOME/bre/conf/netty-transports.yml` file and add the following line to the given interface ID in the listenerConfigurations section. You can enable http2 in multiple interfaces as well, e.g., HTTP and HTTPS.

```
http2: true
```

For example:

```
id: "default"
host: "0.0.0.0"
port: 9090
http2: true
```

### Download the ALPN agent

HTTP/2 over TLS requires the use of ALPN to negotiate the use of the h2 protocol. Java does not currently support ALPN, which will be supported in the next Java version. For lack of support in the JDK we need to use the Jetty-ALPN  bootclasspath extension.

You can download the [jetty-alpn-agent][1] and set the Java agent in the `$BALLERINA_HOME/bin/ballerina` file as follows:

```
-javaagent:(path_to_jetty-alpn-agent.jar)\
```

For example:

```
-javaagent:"$BALLERINA_HOME/bre/lib/jetty-alpn-agent-2.0.6.jar"\
```
>NOTE: The java-agent has all the class path extensions of different JDK versions. But you can use the release of the Jetty-ALPN JAR specific to the version of Java you are using and Xbootclasspath the JVM option referencing the path to the Jetty alpn-boot JAR. See the [Netty documentation][2] for other SSL options.

[1]: https://mvnrepository.com/artifact/org.mortbay.jetty.alpn/jetty-alpn-agent/2.0.6

[2]: http://netty.io/wiki/requirements-for-4.x.html

## Test the HTTP/2 service

You can use an HTTP/2 client to test the HTTP/2 services. See the example [HTTP/2 Java client][3] and [test samples][4] to see how to call HTTP samples using an HTTP/2 Java client.

[3]: https://github.com/ballerinalang/ballerina/tree/master/modules/tests/test-integration/src/test/java/org/ballerinalang/test/util/http2

[4]: https://github.com/ballerinalang/ballerina/tree/master/modules/tests/test-integration/src/test/java/org/ballerinalang/test/service/http2/sample
