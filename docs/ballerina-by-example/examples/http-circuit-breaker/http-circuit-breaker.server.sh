$ ballerina run http-circuit-breaker.bal
ballerina: deploying service(s) in 'http-circuit-breaker.bal'
ballerina: started HTTP/WS server connector 0.0.0.0:9090
ballerina: started HTTP/WS server connector 0.0.0.0:8080
Hello World!!!
Hello World!!!
Internal error occurred while processing the request.
2018-03-04 11:56:24,644 INFO  [ballerina.net.http.resiliency] - CircuitBreaker failure threshold exceeded. Circuit trips from CLOSE to OPEN state.
{message:"Upstream service unavailable. Requests to upstream service will be suspended for -4218 milliseconds.", cause:null, statusCode:0}
2018-03-04 11:56:42,703 INFO  [ballerina.net.http.resiliency] - CircuitBreaker reset timeout reached. Circuit switched from OPEN to HALF_OPEN state.
Hello World!!!
2018-03-04 11:56:52,713 INFO  [ballerina.net.http.resiliency] - CircuitBreaker trial run  was successful. Circuit switched from HALF_OPEN to CLOSE state.
{message:"Idle timeout triggered before reading inbound response", cause:null, statusCode:504}
2018-03-04 11:57:08,696 INFO  [ballerina.net.http.resiliency] - CircuitBreaker failure threshold exceeded. Circuit tripped from CLOSE to OPEN state.
{message:"Upstream service unavailable. Requests to upstream service will be suspended for -3974 milliseconds.", cause:null, statusCode:0}
2018-03-04 11:57:25,913 INFO  [ballerina.net.http.resiliency] - CircuitBreaker reset timeout reached. Circuit switched from OPEN to HALF_OPEN state.
Hello World!!!
2018-03-04 11:57:27,281 INFO  [ballerina.net.http.resiliency] - CircuitBreaker trial run  was successful. Circuit switched from HALF_OPEN to CLOSE state.
Hello World!!!
