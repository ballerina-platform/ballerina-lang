// The order management HTTP service acting as a Ballerina WebSub Publisher brings up an internal Ballerina WebSub Hub
// at which it will publish updates.
import ballerina/http;
import ballerina/log;
import ballerina/websub;

endpoint http:Listener listener {
    port: 9090
};

// The topic against which the publisher will publish updates, and the subscribers
// need to subscribe to, to receive notifications when an order is placed
@final string ORDER_TOPIC = "http://localhost:9090/ordermgt/ordertopic";

// An in-memory map to which orders will be added for demonstration
map<json> orderMap;

// Invoke the function that start up a Ballerina WebSub Hub, register the topic
// against which updates will be published, and maintain a reference to the
// returned hub object to publish updates
websub:WebSubHub webSubHub = startHubAndRegisterTopic();

@http:ServiceConfig {
    basePath: "/ordermgt"
}
service<http:Service> orderMgt bind listener {

    // Resource accepting discovery requests
    // Requests received at this resource would respond with a Link Header
    // indicating the topic to subscribe to and the hub(s) to subscribe at
    @http:ResourceConfig {
        methods: ["GET", "HEAD"],
        path: "/order"
    }
    discoverPlaceOrder(endpoint caller, http:Request req) {
        http:Response response;
        // Add a link header indicating the hub and topic
        websub:addWebSubLinkHeader(response, [webSubHub.hubUrl], ORDER_TOPIC);
        response.statusCode = 202;
        caller->respond(response) but {
            error e => log:printError("Error responding on ordering", err = e)
        };
    }

    // Resource accepting order placement requests
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/order"
    }
    placeOrder(endpoint caller, http:Request req) {
        json orderReq = check req.getJsonPayload();
        string orderId = orderReq.Order.ID.toString();
        orderMap[orderId] = orderReq;

        // Create the response message indicating successful order creation.
        http:Response response;
        response.statusCode = 202;
        caller->respond(response) but {
            error e => log:printError("Error responding on ordering", err = e)
        };

        // Publish the update to the Hub, to notify subscribers
        string orderCreatedNotification = "New Order Added: " + orderId;
        log:printInfo(orderCreatedNotification);
        webSubHub.publishUpdate(ORDER_TOPIC, orderCreatedNotification) but {
            error e => log:printError("Error publishing update", err = e)
        };
    }

}

// Start up a Ballerina WebSub Hub on port 9191 and register the topic against
// which updates will be published
function startHubAndRegisterTopic() returns websub:WebSubHub {
    websub:WebSubHub internalHub = websub:startHub(9191) but {
        websub:HubStartedUpError hubStartedUpErr => hubStartedUpErr.startedUpHub
    };
    internalHub.registerTopic(ORDER_TOPIC) but {
        error e => log:printError("Error registering topic", err = e)
    };
    return internalHub;
}
