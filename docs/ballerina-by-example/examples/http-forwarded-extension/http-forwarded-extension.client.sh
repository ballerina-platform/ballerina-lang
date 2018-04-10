# To invoke the resource, use this client.
$ curl -v http://localhost:9090/proxy/ -H "Forwarded:by=192.2.1.10;host=www.abc.com;proto=http"
# The server response is as follows:
forwarded header value : for=192.2.1.10; by=127.0.0.1; host=www.abc.com; proto=http
