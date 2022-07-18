import ballerina/http;

@choreo:Service { id: "003" }
service /time on new http:Listener(7070) {
    resource function get currentDate() returns string {
        return "21/07/2022";
    }
}
