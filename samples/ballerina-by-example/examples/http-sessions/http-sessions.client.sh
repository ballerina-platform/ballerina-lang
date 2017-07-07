$ curl -v http://localhost:9090/session/sayHello
Say hello to a new session

# Copy the BSESSIONID of session cookie in above response.
# Now invoke welcome resource as follows.
$ curl -v http://localhost:9090/session/welcome
        -H "Cookie: BSESSIONID=..(use above BSESSIONID).."
Hi Session example

# Finally invoke sayBye using same BSESSIONID.
$ curl -v http://localhost:9090/session/sayBye
        -H "Cookie: BSESSIONID=..(use same BSESSIONID).."
Session: ..(same BSESSIONID).. invalidated