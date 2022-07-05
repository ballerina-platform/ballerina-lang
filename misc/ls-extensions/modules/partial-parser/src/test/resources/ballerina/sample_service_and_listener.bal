import ballerina/http;
listener http:Listener l = new (9090);
service / on l {
    resource function get .() returns error? {
    }
}
