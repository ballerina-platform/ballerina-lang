# To invoke sayHello resource, use following client.
$ curl -v http://localhost:9090/session/sayHello
# Server response
Say hello to a new session
# Copy the BSESSIONID of session cookie in above response.
# Now invoke welcome resource as follows.
$ curl -v http://localhost:9090/session/welcome
        -H "Cookie: BSESSIONID=..(use above BSESSIONID).."
# Server response
Hi Session example
# Finally invoke sayBye using same BSESSIONID.
$ curl -v http://localhost:9090/session/sayBye
        -H "Cookie: BSESSIONID=..(use same BSESSIONID).."
# Server response
Session: ..(same BSESSIONID).. invalidated