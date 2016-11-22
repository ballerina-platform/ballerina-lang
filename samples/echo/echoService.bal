@BasePath ("/echo")

@Service(title = "EchoService", description = "Description")

package com.sample;

resource resourceName (message m) {
    reply m;
}

