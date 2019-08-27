import ballerina/http;
import ballerina/io;

type ClientRequest record {
    string host;
};

type RequestCount record {
    string host;
    int count;
};

stream<ClientRequest> requestStream = new;
stream<RequestCount> requestCountStream = new;

function initRealtimeRequestCounter() returns () {

    // Whenever the `requestCountStream` stream receives an event from the streaming rules defined in the `forever` block,
    // the `printRequestCount` function is invoked.
    requestCountStream.subscribe(printRequestCount);

    // Gathers all the events coming in to the `requestStream` for five seconds, groups them by the host, counts the number
    // of requests per host, and checks if the count is more than 6. If yes, publish the output (host and the count) to
    // the `requestCountStream` stream as an alert. This `forever` block is executed once when initializing the service.
    // The processing happens asynchronously each time the `requestStream` receives an event.
    forever {
        from requestStream window timeBatch(10000)
        select requestStream.host, count() as count
            group by requestStream.host
            having count > 6
        => (RequestCount[] counts) {
        // `counts` is the output of the streaming rules and is published to the `requestCountStream`.
        // The `select` clause should match the structure of the `RequestCount` record.
            foreach var c in counts {
                requestCountStream.publish(c);
            }
        }
    }
}

// Defines the `printRequestCount` function.
function printRequestCount(RequestCount reqCount) {
    io:println("ALERT!! : Received more than 6 requests from the " +
                        "host within 10 seconds : " + reqCount.host);
}

listener http:Listener ep = new (9090);

@http:ServiceConfig {
    basePath: "/"
}
// The host header is extracted from the requests that come to the service using the `/requests` context. Using this
// information, the `clientRequest` object is created and published to the `requestStream`.
service requestService on ep {

    // TODO: issue #17267
    () ftr = initRealtimeRequestCounter();

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/requests"
    }
    resource function requests(http:Caller conn, http:Request req) {
        string hostName = <@untainted> conn.remoteAddress.host;
        ClientRequest clientRequest = { host: hostName };
        requestStream.publish(clientRequest);

        http:Response res = new;
        res.setJsonPayload("{'message' : 'request successfully " +
                                "received'}");
        error? result = conn->respond(res);
        if (result is error) {
            io:println("Error in responding to caller", result);
        }
    }
}

