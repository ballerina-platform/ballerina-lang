# To invoke the Failover Service, use following curl command.
$ curl -v http://localhost:9090/fo
# Once you invoke the Failover service, the Failover connector fails over the request to the configured
# Http endpoints. In this example, the third invocation gives a successful response.
# Server response:
Mock Resource is Invoked.
