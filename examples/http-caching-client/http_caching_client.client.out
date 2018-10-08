#Run the following curl command to run the client. 
$ curl -v http://localhost:9090/cache
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cache HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 200 OK
< cache-control: must-revalidate, no-transform, public, max-age=15
< etag: 620328e8
< content-type: application/json
< date: Wed, 28 Mar 2018 21:43:28 +0530
< server: ballerina/0.970.0-alpha1-SNAPSHOT
< content-length: 27
<
* Connection #0 to host localhost left intact
{"message":"Hello, World!"}

$ curl -v http://localhost:9090/cache
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cache HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 200 OK
< cache-control: must-revalidate, no-transform, public, max-age=15
< etag: 620328e8
< content-type: application/json
< date: Wed, 28 Mar 2018 21:43:28 +0530
< age: 14
< server: ballerina/0.970.0-alpha1-SNAPSHOT
< content-length: 27
<
* Connection #0 to host localhost left intact
{"message":"Hello, World!"}

$ curl -v http://localhost:9090/cache
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cache HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 200 OK
< cache-control: must-revalidate, no-transform, public, max-age=15
< etag: 620328e8
< content-type: application/json
< date: Wed, 28 Mar 2018 21:43:28 +0530
< age: 19
< server: ballerina/0.970.0-alpha1-SNAPSHOT
< content-length: 27
<
* Connection #0 to host localhost left intact
{"message":"Hello, World!"}

$ curl -v http://localhost:9090/cache
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cache HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 200 OK
< age: 19
< cache-control: must-revalidate, no-transform, public, max-age=15
< etag: 620328e8
< content-type: application/json
< date: Wed, 28 Mar 2018 21:43:57 +0530
< server: ballerina/0.970.0-alpha1-SNAPSHOT
< content-length: 27
<
* Connection #0 to host localhost left intact
{"message":"Hello, World!"}
