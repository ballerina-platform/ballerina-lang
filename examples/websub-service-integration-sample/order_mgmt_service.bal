// The order management HTTP service acting as a Ballerina WebSub Publisher brings up an internal Ballerina WebSub Hub
// at which it will publish updates.
import ballerina/http;
import ballerina/log;
import ballerina/websub;

listener http:Listener httpListener = new(9090);

// The topic against which the publisher will publish updates and the subscribers
// need to subscribe to, to receive notifications when an order is placed.
final string ORDER_TOPIC = "http://localhost:9090/ordermgt/ordertopic";

// An in-memory `map` to which orders will be added.
map<json> orderMap = {};

// Invokes the function that starts up a Ballerina WebSub Hub, registers the topic
// against which updates will be published, and maintains a reference to the
// returned hub object to publish updates.
websub:Hub webSubHub = startHubAndRegisterTopic();

@http:ServiceConfig {
    basePath: "/ordermgt"
}
service orderMgt on httpListener {

    // This resource accepts the discovery requests.
    // Requests received at this resource would respond with a Link Header
    // indicating the topic to subscribe to and the hub(s) to subscribe at.
    @http:ResourceConfig {
        methods: ["GET", "HEAD"],
        path: "/order"
    }
    resource function discoverPlaceOrder(http:Caller caller, http:Request req) {
        http:Response response = new;
        // Adds a link header indicating the hub and topic.
        websub:addWebSubLinkHeader(response, [webSubHub.subscriptionUrl], ORDER_TOPIC);
        response.statusCode = 202;
        var result = caller->respond(response);
        if (result is error) {
           log:printError("Error responding on ordering", result);
        }
    }

    // This resource accepts order placement requests.
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/order"
    }
    resource function placeOrder(http:Caller caller, http:Request req) {
        var orderReq = req.getJsonPayload();
        if (orderReq is json) {
            string orderId = orderReq.Order.ID.toString();
            orderMap[orderId] = <@untainted> orderReq;

            // Creates the response message indicating successful order creation.
            http:Response response = new;
            response.statusCode = 202;
            var result = caller->respond(response);
            if (result is error) {
               log:printError("Error responding on ordering", result);
            }

            // Publishes the update to the Hub to notify the subscribers.
            string orderCreatedNotification = "New Order Added: " + orderId;
            log:printInfo(orderCreatedNotification);
            var updateResult = webSubHub.publishUpdate(ORDER_TOPIC,
                                                    orderCreatedNotification);
            if (updateResult is error) {
                log:printError("Error publishing update", updateResult);
            }
        } else {
            error e = orderReq;
            log:printError("Error retrieving payload", e);
            panic e;
        }
    }

}

// Starts up a Ballerina WebSub Hub on port 9191 and registers the topic against
// which updates will be published.
function startHubAndRegisterTopic() returns websub:Hub {
    var hubStartUpResult = websub:startHub(new http:Listener(9191), "/websub", "/hub");

    websub:Hub? hubVar = ();
    if hubStartUpResult is websub:HubStartupError {
        panic hubStartUpResult;
    } else {
        hubVar = hubStartUpResult is websub:HubStartedUpError
                            ? hubStartUpResult.startedUpHub : hubStartUpResult;
    }

    websub:Hub internalHub = <websub:Hub> hubVar;
    var result = internalHub.registerTopic(ORDER_TOPIC);
    if (result is error) {
        log:printError("Error registering topic", result);
    }
    return internalHub;
}
