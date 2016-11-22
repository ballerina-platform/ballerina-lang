@BasePath ("/echo")

@Service(title = "EchoService", description = "echo service sample.")

package com.sample;

@GET
@Path ("/*")
resource echoResource (message m) {
    reply m;
}

