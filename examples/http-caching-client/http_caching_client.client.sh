$ curl -v http://localhost:9090/proxy
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /proxy HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Cache-Control: must-revalidate, no-transform, public, max-age=15
< ETag: ec4ac3d0
< content-type: text/plain
< date: Sat, 10 Mar 2018 11:10:13 +0530
< server: wso2-http-transport
< content-length: 13
<
* Connection #0 to host localhost left intact

$ curl -v http://localhost:9090/proxy
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /proxy HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Cache-Control: must-revalidate, no-transform, public, max-age=15
< ETag: ec4ac3d0
< content-type: text/plain
< date: Sat, 10 Mar 2018 11:10:13 +0530
< server: wso2-http-transport
< content-length: 13
<
* Connection #0 to host localhost left intact
Hello, World!

$ curl -v http://localhost:9090/proxy
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /proxy HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Cache-Control: must-revalidate, no-transform, public, max-age=15
< ETag: ec4ac3d0
< content-type: text/plain
< date: Sat, 10 Mar 2018 11:10:13 +0530
< server: wso2-http-transport
< content-length: 13
<
* Connection #0 to host localhost left intact
Hello, World!

$ curl -v http://localhost:9090/proxy
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /proxy HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Cache-Control: must-revalidate, no-transform, public, max-age=15
< ETag: ec4ac3d0
< content-type: text/plain
< date: Sat, 10 Mar 2018 11:10:13 +0530
< server: wso2-http-transport
< content-length: 13
<
* Connection #0 to host localhost left intact
Hello, World!