import ballerina/io;
//Defines a channel that accepts xml messages.
channel<xml> xmlChn;

public function main(string... args) {

    worker w1 {
        map key = {myKey:"abc123"};
        io:println("W1 Waiting for a message...");
        xml message;
        //Waiting to receive a message with key from xmlChn channel.
        message <- xmlChn, key;
        io:println("W1 Received message: ", message);
        io:println("W1 Waiting for a null key message...");
        //Waiting to receive a message without key from xmlChn channel.
        message <- xmlChn;
        io:println("W1 Received message: ", message);
    }

    worker w2 {
        map key = {myKey:"abc123"};
        xml msg = xml `<msg><name>ballerina</name><worker>w2</worker></msg>`;
        //Sending a message to the xmlChn, with key.
        msg -> xmlChn, key;
        io:println("Sent message from w2");
    }

    worker w3 {
        xml msg = xml `<msg><name>ballerina</name><worker>w3</worker></msg>`;
        //Sending a message to the xmlChn, without a key.
        msg -> xmlChn;
        io:println("Sent message from w3");
    }
}
