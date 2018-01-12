$ curl -v -d "TEST 100 CONTINUE" -H'Expect:100-continue' http://localhost:9090/hello
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 9090 (#0)
> POST /hello HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.47.0
> Accept: */*
> Expect:100-continue
> Content-Length: 17
> Content-Type: application/x-www-form-urlencoded
>
< HTTP/1.1 100 Continue
* We are completely uploaded and fine
< HTTP/1.1 200 OK
< Content-Type: text/plain
< Content-Length: 13
<
Hello World!
* Connection #0 to host localhost left intact