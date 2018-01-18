$ ballerina run http-circuit-breaker.bal
ballerina: deploying service(s) in 'http-circuit-breaker.bal'
ballerina: started HTTP/WS server connector 0.0.0.0:9090
Hello World!!!
Hello World!!!
{msg:"Gateway Timeout", cause:null, stackTrace:null, statusCode:504}
Upstream service unavailable. Requests to upstream service will be suspended for 14061 milliseconds
Upstream service unavailable. Requests to upstream service will be suspended for 5398 milliseconds
Upstream service unavailable. Requests to upstream service will be suspended for 1753 milliseconds
Hello World!!!
Hello World!!!
