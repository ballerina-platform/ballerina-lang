# The two services shown above has to be run separately to observe the following output. For clarity, only the relevant parts of the HTTP trace logs has been included here.
$ ballerina run caching_proxy.bal -B[tracelog.http].level=TRACE
ballerina: deploying service(s) in 'caching_proxy.bal'
ballerina: started HTTP/WS server connector 0.0.0.0:9090
# The caching proxy receives a request from a client.
[2018-03-10 11:10:12,867] TRACE {tracelog.http.downstream} - [id: 0x190c00fc, correlatedSource: n/a, host:/127.0.0.1:9090 - remote:/127.0.0.1:35302] INBOUND: DefaultHttpRequest(decodeResult: success, version: HTTP/1.1)
GET /proxy HTTP/1.1
Host: localhost:9090
User-Agent: curl/7.47.0
Accept: */*

# The proxy in turn makes a request to the backend service.
[2018-03-10 11:10:12,926] TRACE {tracelog.http.upstream} - [id: 0x1c7c4ad1, correlatedSource: 0x190c00fc, host:/127.0.0.1:50642 - remote:localhost/127.0.0.1:8080] OUTBOUND: DefaultHttpRequest(decodeResult: success, version: HTTP/1.1)
GET /hello HTTP/1.1
host: localhost:8080
user-agent: ballerina/0.963.1-SNAPSHOT
accept-encoding: deflate, gzip

# The backend service responds with a 200 OK and it contains ETag and Cache-Control headers. This response can be cached and as such, the caching client caches it. As it can be seen from the max-age directive of the Cache-Control header, this response is valid for 15 seconds.
[2018-03-10 11:10:13,221] TRACE {tracelog.http.upstream} - [id: 0x1c7c4ad1, correlatedSource: 0x190c00fc, host:/127.0.0.1:50642 - remote:localhost/127.0.0.1:8080] INBOUND: DefaultHttpResponse(decodeResult: success, version: HTTP/1.1)
HTTP/1.1 200 OK
Cache-Control: must-revalidate, no-transform, public, max-age=15
ETag: ec4ac3d0
content-type: text/plain
server: wso2-http-transport
date: Sat, 10 Mar 2018 11:10:13 +0530
transfer-encoding: chunked
[2018-03-10 11:10:13,237] TRACE {tracelog.http.upstream} - [id: 0x1c7c4ad1, correlatedSource: 0x190c00fc, host:/127.0.0.1:50642 - remote:localhost/127.0.0.1:8080] INBOUND: DefaultHttpContent(data: PooledUnsafeHeapByteBuf(ridx: 0, widx: 13, cap: 58), decoderResult: success), 13B
Hello, World!

# The response is sent back to the client.
[2018-03-10 11:10:13,254] TRACE {tracelog.http.downstream} - [id: 0x190c00fc, correlatedSource: n/a, host:localhost/127.0.0.1:9090 - remote:/127.0.0.1:35302] OUTBOUND: DefaultFullHttpResponse(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 13, cap: 13, components=1))
HTTP/1.1 200 OK
Cache-Control: must-revalidate, no-transform, public, max-age=15
ETag: ec4ac3d0
content-type: text/plain
date: Sat, 10 Mar 2018 11:10:13 +0530
server: wso2-http-transport
content-length: 13, 13B
Hello, World!

# Subsequent requests to the proxy within the next 15 seconds are served from the proxy's cache. As it can be seen here, the backend service is not contacted.
[2018-03-10 11:10:24,700] TRACE {tracelog.http.downstream} - [id: 0xd5e19864, correlatedSource: n/a, host:/127.0.0.1:9090 - remote:/127.0.0.1:35306] INBOUND: DefaultHttpRequest(decodeResult: success, version: HTTP/1.1)
GET /proxy HTTP/1.1
Host: localhost:9090
User-Agent: curl/7.47.0
Accept: */*

# Cached response.
[2018-03-10 11:10:24,728] TRACE {tracelog.http.downstream} - [id: 0xd5e19864, correlatedSource: n/a, host:localhost/127.0.0.1:9090 - remote:/127.0.0.1:35306] OUTBOUND: DefaultFullHttpResponse(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 13, cap: 13, components=1))
HTTP/1.1 200 OK
Cache-Control: must-revalidate, no-transform, public, max-age=15
ETag: ec4ac3d0
content-type: text/plain
date: Sat, 10 Mar 2018 11:10:13 +0530
server: wso2-http-transport
content-length: 13, 13B
Hello, World!

# Another request which results in a cached response being served.
[2018-03-10 11:10:59,418] TRACE {tracelog.http.downstream} - [id: 0x6b1b00da, correlatedSource: n/a, host:/127.0.0.1:9090 - remote:/127.0.0.1:35308] INBOUND: DefaultHttpRequest(decodeResult: success, version: HTTP/1.1)
GET /proxy HTTP/1.1
Host: localhost:9090
User-Agent: curl/7.47.0
Accept: */*

[2018-03-10 11:10:59,445] TRACE {tracelog.http.downstream} - [id: 0x6b1b00da, correlatedSource: n/a, host:localhost/127.0.0.1:9090 - remote:/127.0.0.1:35308] OUTBOUND: DefaultFullHttpResponse(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 13, cap: 13, components=1))
HTTP/1.1 200 OK
Cache-Control: must-revalidate, no-transform, public, max-age=15
ETag: ec4ac3d0
content-type: text/plain
date: Sat, 10 Mar 2018 11:10:13 +0530
server: wso2-http-transport
content-length: 13, 13B
Hello, World!

# Another request sent is after staying idle for a while.
[2018-03-10 11:11:38,157] TRACE {tracelog.http.downstream} - [id: 0xe3525b35, correlatedSource: n/a, host:/127.0.0.1:9090 - remote:/127.0.0.1:35316] INBOUND: DefaultHttpRequest(decodeResult: success, version: HTTP/1.1)
GET /proxy HTTP/1.1
Host: localhost:9090
User-Agent: curl/7.47.0
Accept: */*

# As it can be seen, this time the request is not served from the cache. The backend service is contacted. The If-None-Match header sends the entity tag of the now stale response, so that the backend service may determine whether this response is still valid.
[2018-03-10 11:11:38,164] TRACE {tracelog.http.upstream} - [id: 0x1c7c4ad1, correlatedSource: 0xe3525b35, host:/127.0.0.1:50642 - remote:localhost/127.0.0.1:8080] OUTBOUND: DefaultHttpRequest(decodeResult: success, version: HTTP/1.1)
GET /hello HTTP/1.1
If-None-Match: ec4ac3d0
host: localhost:8080
user-agent: ballerina/0.963.1-SNAPSHOT
accept-encoding: deflate, gzip

# The response has not changed. Therefore the backend services responds with a 304 Not Modified response. Based on this, the proxy will refresh the response, so that it can continue serving the cached response.
[2018-03-10 11:11:38,173] TRACE {tracelog.http.upstream} - [id: 0x1c7c4ad1, correlatedSource: 0xe3525b35, host:/127.0.0.1:50642 - remote:localhost/127.0.0.1:8080] INBOUND: DefaultHttpResponse(decodeResult: success, version: HTTP/1.1)
HTTP/1.1 304 Not Modified
Cache-Control: must-revalidate, no-transform, public, max-age=15
ETag: ec4ac3d0
content-type: text/plain
content-length: 0
server: wso2-http-transport
date: Sat, 10 Mar 2018 11:11:38 +0530

# The cached response is served yet again, since the response has not changed.
[2018-03-10 11:11:38,177] TRACE {tracelog.http.downstream} - [id: 0xe3525b35, correlatedSource: n/a, host:localhost/127.0.0.1:9090 - remote:/127.0.0.1:35316] OUTBOUND: DefaultFullHttpResponse(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 13, cap: 13, components=1))
HTTP/1.1 200 OK
Cache-Control: must-revalidate, no-transform, public, max-age=15
ETag: ec4ac3d0
content-type: text/plain
date: Sat, 10 Mar 2018 11:10:13 +0530
server: wso2-http-transport
content-length: 13, 13B
Hello, World!

# The output for the mock service.
$ ballerina run hello_service.bal -B[tracelog.http].level=TRACE
ballerina: deploying service(s) in 'hello_service.bal'
ballerina: started HTTP/WS server connector 0.0.0.0:8080
# For the first request the caching proxy receives, it sends a request to the hello service.
[2018-03-10 11:10:13,009] TRACE {tracelog.http.downstream} - [id: 0xe79c407e, correlatedSource: n/a, host:/127.0.0.1:8080 - remote:/127.0.0.1:50642] INBOUND: DefaultHttpRequest(decodeResult: success, version: HTTP/1.1)
GET /hello HTTP/1.1
host: localhost:8080
user-agent: ballerina/0.963.1-SNAPSHOT
accept-encoding: deflate, gzip

# The service responds with a 200 OK with relevant caching headers set.
[2018-03-10 11:10:13,052] TRACE {tracelog.http.downstream} - [id: 0xe79c407e, correlatedSource: n/a, host:localhost/127.0.0.1:8080 - remote:/127.0.0.1:50642] OUTBOUND: DefaultFullHttpResponse(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 13, cap: 13, components=1))
HTTP/1.1 200 OK
Cache-Control: must-revalidate, no-transform, public, max-age=15
ETag: ec4ac3d0
content-type: text/plain
content-length: 13
server: wso2-http-transport
date: Sat, 10 Mar 2018 11:10:13 +0530, 13B
Hello, World!
[2018-03-10 11:10:13,122] TRACE {tracelog.http.downstream} - [id: 0xe79c407e, correlatedSource: n/a, host:localhost/127.0.0.1:8080 - remote:/127.0.0.1:50642] FLUSH

# The backend service only gets another request when the cached response the proxy has expired and it wants to validate it again.
[2018-03-10 11:11:38,167] TRACE {tracelog.http.downstream} - [id: 0xe79c407e, correlatedSource: n/a, host:localhost/127.0.0.1:8080 - remote:/127.0.0.1:50642] INBOUND: DefaultHttpRequest(decodeResult: success, version: HTTP/1.1)
GET /hello HTTP/1.1
If-None-Match: ec4ac3d0
host: localhost:8080
user-agent: ballerina/0.963.1-SNAPSHOT
accept-encoding: deflate, gzip

# After taking a look at the If-None-Match header, the service determines that the response is still the same and that the proxy can keep reusing it.
[2018-03-10 11:11:38,170] TRACE {tracelog.http.downstream} - [id: 0xe79c407e, correlatedSource: n/a, host:localhost/127.0.0.1:8080 - remote:/127.0.0.1:50642] OUTBOUND: DefaultFullHttpResponse(decodeResult: success, version: HTTP/1.1, content: CompositeByteBuf(ridx: 0, widx: 0, cap: 0, components=1))
HTTP/1.1 304 Not Modified
Cache-Control: must-revalidate, no-transform, public, max-age=15
ETag: ec4ac3d0
content-type: text/plain
content-length: 0
server: wso2-http-transport
date: Sat, 10 Mar 2018 11:11:38 +0530, 0B
