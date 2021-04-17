import ballerina/http;
import ballerina/io;
import ballerina/websocket;

string NAME = "name";
string AGE = "age";
string ageValue = "";
string nameValue = "";

// Stores the connection IDs of users who join the chat.
map<websocket:Caller> connectionsMap = {};

// Upgrade from HTTP to websocket. To upgrade to a websocket connection you
// can return a `websocket:Service` or to cancel the websocket upgrade you can
// return a `websocket:UpgradeError`
service /chat on new websocket:Listener(9090) {
    resource function get [string name](http:Request req) returns
                         websocket:Service|websocket:UpgradeError {
        // Retrieve query parameters from the [http:Request](https://ballerina.io/swan-lake/learn/api-docs/ballerina/#/ballerina/http/latest/http/classes/Request).
        map<string[]> queryParams = req.getQueryParams();
        if (!queryParams.hasKey(AGE)) {
            // Cancel the handshake by sending an UpgradeError
            // if the age parameter is missing in the request.
            websocket:UpgradeError upgradeErr = error
                                    websocket:UpgradeError("Age is required");
            return upgradeErr;
        } else {
            string? ageQueryParam = req.getQueryParamValue(AGE);
            ageValue = <@untainted>
                         (ageQueryParam is string ? ageQueryParam : "");
            nameValue = name;
            return service object websocket:Service {
                // Once a user connects to the chat, store the attributes
                // of the user, such as username and age, and broadcast
                // that the user has joined the chat.
                remote function onOpen(websocket:Caller caller) {
                    string welcomeMsg = "Hi " + nameValue
                           + "! You have successfully connected to the chat";
                    var err = caller->writeTextMessage(welcomeMsg);
                    if (err is websocket:Error) {
                        io:println("Error sending message:" + err.message());
                    }
                    string msg = nameValue + " with age " + ageValue
                            + " connected to chat";
                    broadcast(msg);
                    // The attributes of the caller is useful for storing
                    // connection-specific data. In this case, the `NAME`
                    // and `AGE` are unique to each connection
                    caller.setAttribute(NAME, nameValue);
                    caller.setAttribute(AGE, ageValue);
                    connectionsMap[caller.getConnectionId()] =
                                            <@untainted>caller;
                }
                // Broadcast the messages sent by a user.
                remote function onTextMessage(websocket:Caller caller,
                                          string text) {
                    string msg = getAttributeStr(caller, NAME) + ": " + text;
                    io:println(msg);
                    broadcast(msg);
                }
                // Broadcast that a user has left the chat once a user leaves
                // the chat.
                remote function onClose(websocket:Caller caller,
                         int statusCode, string reason) {
                    _ = connectionsMap.remove(caller.getConnectionId());
                    string msg = getAttributeStr(caller, NAME)
                                    + " left the chat";
                    broadcast(msg);
                }
            };
        }
    }
}

// Function to perform the broadcasting of text messages.
function broadcast(string text) {
    foreach var con in connectionsMap {
        var err = con->writeTextMessage(text);
        if (err is websocket:Error) {
            io:println("Error sending message:" + err.message());
        }
    }
}

function getAttributeStr(websocket:Caller ep, string key)
returns (string) {
    var name = ep.getAttribute(key);
    return name.toString();
}
