# To invoke the Failover Service, use following curl command.
$ curl -v http://localhost:9090/failoverService
# Once you invoke the Failover service, Failover connector will failover the request to the configured
# HttpClient connectors. In this example third connector will give a success response.
# Server response:
Mock Resource is Invoked.
