// This is the client implementation of the bidirectional streaming scenario.
import ballerina/grpc;
import ballerina/io;

public function main (string... args) returns error? {
    // Client endpoint configuration.
    ChatClient ep = check new("http://localhost:9090");
    // Executes the RPC call and receives the customized streaming client.
    ChatStreamingClient streamingClient = check ep->chat();

    // Sends multiple messages to the server.
    ChatMessage[] messages = [
        {name: "Sam", message: "Hi"},
        {name: "Ann", message: "Hey"},
        {name: "John", message: "Hello"}
    ];
    foreach ChatMessage msg in messages {
        check streamingClient->send(msg);
    }
    // Once all the messages are sent, the client sends the message to notify the server about the completion.
    check streamingClient->complete();
    // Receives the server stream response iteratively.
    var result = streamingClient->receive();
    while !(result is grpc:EOS) {
        if !(result is grpc:Error) {
            [anydata, map<string|string[]>][value, _] =
            <[anydata, map<string|string[]>]> result;
            io:println(<[anydata, map<string|string[]>]> result);
        }
        result = streamingClient->receive();
    }
}

public client class ChatClient {

    *grpc:AbstractClientEndpoint;

    private grpc:Client grpcClient;

    public isolated function init(string url,
    grpc:ClientConfiguration? config = ()) returns grpc:Error? {
        // Initialize the client endpoint.
        self.grpcClient = check new(url, config);
        checkpanic self.grpcClient.initStub(self, ROOT_DESCRIPTOR,
        getDescriptorMap());
    }

    isolated remote function chat() returns (ChatStreamingClient|grpc:Error) {
        grpc:StreamingClient sClient = check
        self.grpcClient->executeBidirectionalStreaming("Chat/chat");
        return new ChatStreamingClient(sClient);
    }
}

public client class ChatStreamingClient {
    private grpc:StreamingClient sClient;

    isolated function init(grpc:StreamingClient sClient) {
        self.sClient = sClient;
    }

    isolated remote function send(ChatMessage message) returns grpc:Error? {

        return self.sClient->send(message);
    }

    isolated remote function receive() returns anydata|grpc:Error {
        return self.sClient->receive();
    }

    isolated remote function sendError(grpc:Error response)
                                returns grpc:Error? {
        return self.sClient->sendError(response);
    }

    isolated remote function complete() returns grpc:Error? {
        return self.sClient->complete();
    }
}

public type ChatMessage record {|
    string name = "";
    string message = "";
|};
