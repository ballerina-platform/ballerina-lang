$ ballerina run http-circuit-breaker.bal
ballerina: deploying service(s) in 'http-circuit-breaker.bal'
ballerina: started HTTP/WS server connector 0.0.0.0:9090
Hello World!!!
Hello World!!!
{msg:"Gateway Timeout", cause:null, stackTrace:null, statusCode:504}
{msg:"Upstream service unavailable. Requests to upstream service will be suspended for 14309 milliseconds.", cause:null, stackTrace:null, statusCode:0}
{msg:"Upstream service unavailable. Requests to upstream service will be suspended for 6978 milliseconds.", cause:null, stackTrace:null, statusCode:0}
{msg:"Upstream service unavailable. Requests to upstream service will be suspended for 3598 milliseconds.", cause:null, stackTrace:null, statusCode:0}
Hello World!!!
Hello World!!!
