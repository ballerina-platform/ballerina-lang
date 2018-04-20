# To invoke the Failover Service, use following curl command.
$ curl -v http://localhost:9090/fo
# Here FailoverClient endpoint configured with three target services where first two targets configured to mimic
# failure backends. In case one target service goes down failover client automatically calls other configured targets.
# Once you invoke the Failover demo service, the Failover client fails over the request to the configured
# target endpoints. In this example, Third target is configured to give successful request and you get below response
# when you invoke the failover demo service.
# Server response:
Mock Resource is Invoked.
