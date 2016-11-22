@BasePath ("/echo")

@Service(title = "EchoService", description = "echo service sample.")

package com.sample;

resource echoResource (message m) {
    reply m;
}

