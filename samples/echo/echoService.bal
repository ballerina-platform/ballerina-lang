@BasePath ("/echo")

@Service(title = "EchoService", description = "Description")

package com.sample;

resource echoResource (message m) {
    reply m;
}

