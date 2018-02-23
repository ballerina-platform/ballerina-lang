# To invoke the Load Balancer Service, use following curl command.
$ curl -v http://localhost:9090/loadBalancerService
# Server response:
Mock1 Resource is invoked.

# Repeat same curl command again to invoke Load Balance Service again.
# Request will be load balanced to the second mock service.
$ curl -v http://localhost:9090/loadBalancerService
# Server response:
Mock2 Resource is invoked.

# Invoke the Load Balancer Service one more time using same curl command.
# Request will be load balanced to the third mock service.
$ curl -v http://localhost:9090/loadBalancerService
# Server response:
Mock3 Resource is invoked.

# One last time invoke the Load Balancer Service using same curl command.
# Request will be load balanced to the first mock service again.
$ curl -v http://localhost:9090/loadBalancerService
# Server response:
Mock1 Resource is invoked.
