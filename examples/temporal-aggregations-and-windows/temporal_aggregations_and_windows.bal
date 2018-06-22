import ballerina/http;
import ballerina/mime;
import ballerina/io;

type ClientRequest record {
    string host;
};

type RequestCount record {
    string host;
    int count;
};

stream<ClientRequest> requestStream;

function initRealtimeRequestCounter() {

    stream<RequestCount> requestCountStream;

    // Whenever the `requestCountStream` stream receives an event from the streaming rules defined in the `forever` block,
    // the `printRequestCount` function is invoked.
    requestCountStream.subscribe(printRequestCount);

    // Gather all the events coming in to the `requestStream` for five seconds, group them by the host, count the number
    // of requests per host, and check if the count is more than six. If yes, publish the output (host and the count) to
    // the `requestCountStream` stream as an alert. This `forever` block is executed once, when initializing the service.
    // The processing happens asynchronously each time the `requestStream` receives an event.
    forever {
        from requestStream
        window timeBatch(5000)
        select host, count(host) as count
        group by host
        having count > 6
        => (RequestCount[] counts) {
        // `counts` is the output of the streaming rules and is published to the `requestCountStream`.
        // The `select` clause should match the structure of the `RequestCount` struct.
            requestCountStream.publish(counts);
        }
    }
}

// Define the `printRequestCount` function.
function printRequestCount(RequestCount reqCount) {
    io:println("ALERT!! : Received more than 6 requests from the " +
                        "host within 5 seconds : " + reqCount.host);
}

endpoint http:Listener ep {
    port: 9090
};

@http:ServiceConfig {
    basePath: "/"
}
// The host header is extracted from the requests that come to the service using the `/requests` context. Using this
// information, the `clientRequest` object is created and published to the `requestStream`.
service requestService bind ep {

    future ftr = start initRealtimeRequestCounter();

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/requests"
    }
    requests(endpoint conn, http:Request req) {
        string hostName = untaint req.getHeader("Host");
        ClientRequest clientRequest = { host: hostName };
        requestStream.publish(clientRequest);

        http:Response res = new;
        res.setJsonPayload("{'message' : 'request successfully " +
                                "received'}");
        _ = conn->respond(res);
    }
}

