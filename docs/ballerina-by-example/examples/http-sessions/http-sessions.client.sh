$ curl -v http://localhost:9090/sessionTest/sayHello
< HTTP/1.1 200 OK
< Content-Type: text/plain
< Set-Cookie: BSESSIONID=D844B4AAC699........E4E249BE5F21; Path=/session;
< Content-Length: 26

Say hello to a new session

# Copy the 'BSESSIONID' of the session cookie in the above response.
# Then invoke the 'doTask' resource as follows.
$ curl -v http://localhost:9090/sessionTest/doTask -H "Cookie: BSESSIONID=..(use given BSESSIONID).."
Session sample

# Finally, invoke 'sayBye' with the same 'BSESSIONID'.
$ curl -v http://localhost:9090/sessionTest/sayBye -H "Cookie: BSESSIONID=..(use same BSESSIONID).."
Session: ..(same BSESSIONID).. invalidated