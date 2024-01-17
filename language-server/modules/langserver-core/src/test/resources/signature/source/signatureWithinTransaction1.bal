import ballerina/httpx;

function initiateNestedTransactionInRemote(string nestingMethod) returns string {
   httpx:Client remoteEp = new("http://localhost:8889");
    string s = "";
    transaction {
        string testString = "Hello World!!";
        testString.indexOf()
    } onretry {
        s += " onretry";
    } committed {
        s += " committed";
    } aborted {
        s += " aborted";
    }
    return s;
}
