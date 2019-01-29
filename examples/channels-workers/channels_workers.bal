import ballerina/io;

// Defines a `channel` that accepts `xml` messages.
channel<xml> xmlChn = new;

public function main(string... args) {

    worker w1 {
        map<string> keyMap = { myKey: "abc123" };
        io:println("w1 Waiting for a message...");
        xml message;
        // Waiting to receive a message with key from xmlChn channel.
        message = <- xmlChn, keyMap;
        io:println("w1 Received message: ", message);
        io:println("w1 Waiting for a null key message...");
        // Wait to receive a message without a key from the xmlChn channel.
        message = <- xmlChn;
        io:println("w1 Received message: ", message);
    }

    worker w2 {
        map<string> keyMap = { myKey: "abc123" };
        xml msg = xml `<msg><name>ballerina</name><worker>w2</worker></msg>`;
        // Send a message with a key to xmlChn.
        msg -> xmlChn, keyMap;
        io:println("Sent message from w2");
    }

    worker w3 {
        xml msg = xml `<msg><name>ballerina</name><worker>w3</worker></msg>`;
        // Send a message without a key to xmlChn.
        msg -> xmlChn;
        io:println("Sent message from w3");
    }

    wait w1;
}
