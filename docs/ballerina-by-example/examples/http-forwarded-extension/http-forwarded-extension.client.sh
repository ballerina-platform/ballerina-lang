# To invoke the resource, use following client.
$ curl -v http://localhost:9090/proxy/ -H "Forwarded:by=192.2.1.10;host=www.abc.com;proto=http"
# Server response:
forwarded header value : for=192.2.1.10; by=127.0.0.1; host=www.abc.com; proto=http
