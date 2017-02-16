#HTTP/2 Server Connector

##HTTP/2 ?
The Hypertext Transfer Protocol (HTTP) is a wildly successful protocol,  but still has some weaknesses with HTTP 1.0 and 1.1 versions.
HTTP/2 is introduced to address those drawbacks with features of Binary information exchange, compressed headers,multiplexing, Server Push.


##HTTP/2 Server Connector
HTTP/2 server connector provides Ballerina programmers to expose their apis for http/2 clients . So when you send HTTP request using
HTTP/2 client , server will upgrade the protocol to HTTP/2 and send response as below.

<br/>
HTTP/1.1 101 Switching Protocols

Connection: upgrade

upgrade: h2c

HTTP/2.0 200

http-to-http2-upgrade:true
<br/>

Then server will send the response to user request .

##How to define a HTTP/2 Service

You can design service same as HTTP Service and deploy in the ballerina server.
Service will be served with both HTTP/1 and HTTP/2 clients after enabling HTTP/2 Support.

HTTP/1 > Ballerina > response with HTTP/1

HTTP/2 > Ballerina > response with HTTP/2

HTTPS  > Ballerina >  response with HTTP/1

HTTPS/2 > Ballerina >  response with HTTP/2


###HTTP/2 Multiplexing
HTTP/2 Multiplexing provides users to send multiple requests with same TCP connection using multiple streams.
One stream will have request-response model.

Example HTTP/2 Java client can be found from [1] and Test Samples from [2] in Test HTTP/2 service section. Thos test samples provide how single HTTPClient connection has been used in multiple requests.

##Enable HTTP/2 Transport

HTTP/2 is disabled by default.In order to initiate HTTP/2  please follow given instructions below.

###Step 1
Open  $BALLERINA_HOME/bre/conf/netty-transports.yml file and add following line to given interface id in listenerConfigurations section. You can set http2 enable in multiple interfaces as well.

Ex.  -
  id: "default"
  host: "0.0.0.0"
  port: 9092
  http2: true


###Step 2

HTTP/2 over TLS  requires the use of ALPN to negotiate the use of the h2 protocol. Java does not currently support ALPN which will be supported next java version.
For lack of support in the JDK we need to use the Jetty-ALPN  bootclasspath extension.
You can download the jetty-alpn-agent from [1] and set the java agent in $BALLERINA_HOME/bin/ballerina file as bellow.

<br/>
-javaagent:"path_to_jetty-alpn-agent.jar"

<br/>
ex.
<br/>
-javaagent:"$BALLERINA_HOME/bre/lib/jetty-alpn-agent-2.0.6.jar" \
<br/>

Note: java-agent has all the class path extensions of diffrent jdk versions.But you can use the release of the Jetty-ALPN jar specific to the version of Java you are using and Xbootclasspath JVM option referencing the path to the Jetty alpn-boot jar.
Please refer [2] for other ssl options.

[1] https://mvnrepository.com/artifact/org.mortbay.jetty.alpn/jetty-alpn-agent/2.0.6

[2] http://netty.io/wiki/requirements-for-4.x.html

##Test HTTP/2 Service

You can use HTTP/2 client to test the HTTP/2 services. Simple Java client can be found in [1] and [2] contains few code
samples how to call HTTP samples using HTTP/2 Java client.

[1] https://github.com/ballerinalang/ballerina/tree/master/modules/tests/testintegration/src/test/java/org/wso2/ballerina/test/util/http2

[2] https://github.com/ballerinalang/ballerina/tree/master/modules/tests/testintegration/src/test/java/org/wso2/ballerina/test/service/http2/sample


