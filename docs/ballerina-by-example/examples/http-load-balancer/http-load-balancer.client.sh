# To invoke the Load Balancer Service, issue this cURL command.
$ curl -v http://localhost:9090/loadBalancerService
# The server response:
Mock1 Resource is invoked.

# Repeat the same cURL command to invoke the Load Balance Service again.
# Request is load balanced to the second mock service.
$ curl -v http://localhost:9090/loadBalancerService
# The server response:
Mock2 Resource is invoked.

# Invoke the Load Balancer Service once more by issuing the same cURL command.
# Request is load balanced to the third mock service.
$ curl -v http://localhost:9090/loadBalancerService
# The server response:
Mock3 Resource is invoked.

# To invoke the Load Balancer Service for the last time, issue the same cURL command.
# Request is load balanced to the first mock service again.
$ curl -v http://localhost:9090/loadBalancerService
# The server response:
Mock1 Resource is invoked.
