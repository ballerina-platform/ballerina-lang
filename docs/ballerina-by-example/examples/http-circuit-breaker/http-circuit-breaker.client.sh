# The first 2 requests complete without any issue.
$ curl -v http://localhost:9090/cb
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cb HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Content-Length: 14
< Content-Type: text/plain
<
* Connection #0 to host localhost left intact
Hello World!!!

$ curl -v http://localhost:9090/cb
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cb HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Content-Length: 14
< Content-Type: text/plain
<
* Connection #0 to host localhost left intact
Hello World!!!

# The third one responds with 500 Internal Server Error, since our mock service send 500 http status code
# when responding to every third request.
$ curl -v http://localhost:9090/cb
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cb HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 500 Internal Server Error
< Connection: Keep-Alive
< Content-Type: text/plain
< Content-Length: 53
<
* Connection #0 to host localhost left intact
Internal error occurred while processing the request.

# Subsequent requests fail immediately since the reset timeout period has not elapsed.
$ curl -v http://localhost:9090/cb
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cb HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 500 Internal Server Error
< Connection: Keep-Alive
< Content-Type: text/plain
< Content-Length: 100
<
* Connection #0 to host localhost left intact
Upstream service unavailable. Requests to upstream service will be suspended for 14061 milliseconds

$ curl -v http://localhost:9090/cb
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cb HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 500 Internal Server Error
< Connection: Keep-Alive
< Content-Type: text/plain
< Content-Length: 99
<
* Connection #0 to host localhost left intact
Upstream service unavailable. Requests to upstream service will be suspended for 5398 milliseconds

$ curl -v http://localhost:9090/cb
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cb HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 500 Internal Server Error
< Connection: Keep-Alive
< Content-Type: text/plain
< Content-Length: 99
<
* Connection #0 to host localhost left intact
Upstream service unavailable. Requests to upstream service will be suspended for 1753 milliseconds

# The request sent immediately after the timeout period expires is the trial request, to see if the backend service is back to normal.
# If this succeeds, the circuit is set to 'close' and normal functionality resumes.
$ curl -v http://localhost:9090/cb
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cb HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Content-Length: 14
< Content-Type: text/plain
<
* Connection #0 to host localhost left intact
Hello World!!!

# The fifth one times out, since our mock service times out when responding to every fifth request.
$ curl -v http://localhost:9090/cb
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cb HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 500 Internal Server Error
< Connection: Keep-Alive
< Content-Type: text/plain
< Content-Length: 54
<
* Connection #0 to host localhost left intact
Idle timeout triggered before reading inbound response

# Subsequent requests fail immediately since the reset timeout period has not elapsed.
$ curl -v http://localhost:9090/cb
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cb HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 500 Internal Server Error
< Connection: Keep-Alive
< Content-Type: text/plain
< Content-Length: 100
<
* Connection #0 to host localhost left intact
Upstream service unavailable. Requests to upstream service will be suspended for 14061 milliseconds

# The request sent immediately after the timeout period expires is the trial request, to see if the backend service is back to normal.
# If this succeeds, the circuit is set to 'close' and normal functionality resumes.
$ curl -v http://localhost:9090/cb
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cb HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Content-Length: 14
< Content-Type: text/plain
<
* Connection #0 to host localhost left intact
Hello World!!!

# Since the immediate request after the timeout expired was successful, the requests after that complete normally.
$ curl -v http://localhost:9090/cb
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> GET /cb HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Content-Length: 14
< Content-Type: text/plain
<
* Connection #0 to host localhost left intact
Hello World!!!
