import ballerina/httpx;

service / on new httpx:Listener(8080) {
    resource function get hello(string page) {
        int pageNumber = page;
    }
}
