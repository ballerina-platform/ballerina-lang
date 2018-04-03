# To invoke the Failover Service, use following curl command.
$ curl -v http://localhost:9090/fo
# Once you invoke the Failover service, Failover connector will failover the request to the configured
# HttpClients. In this example third invocation will give a success response.
# Server response:
Mock Resource is Invoked.
