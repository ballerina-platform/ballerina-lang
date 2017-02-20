package foo.bar;

import ballerina.lang.system;

@doc:Description("Add HTTP header to the message")
@doc:Param("m: Incoming message")
@doc:Param("key: HTTP header key")
@doc:Param("value: HTTP header value")
function addHeader (message m, string key, string value) {
    system:println("invoked");
}

@doc:Description("Get HTTP header from the message")
@doc:Param("m: Incoming message")
@doc:Param("key: HTTP header key")
@doc:Return("HTTP header value")
function getHeader (message m, string key) (string) {
    system:println("invoked");
    return "value";
}

@doc:Description("an Argument")
@doc:Field("text: a string")
@doc:Field("argumentId: an id")
@doc:Field("sentiment: setiment about the argument")
struct Argument {
    string text;
    int argumentId;
    int sentiment;
}

@doc:Description("Test connector")
@doc:Param("consumerKey: consumer key")
@doc:Param("consumerSecret: consumer secret")
@doc:Param("accessToken: access token")
@doc:Param("accessTokenSecret: access token secret")
connector TestConnector(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    @doc:Description("test connector action")
    @doc:Param("t: connector object")
    @doc:Param("msg: a string message")
    @doc:Return("response: response object")
    action testAction1(TestConnector t, string msg) (message response) {
        message request;
        response = request;
        return response;
    }

    @doc:Description("test connector action2")
    @doc:Param("t: connector object")
    @doc:Param("msg: a string message")
    @doc:Return("response2: response object")
    action testAction2(TestConnector t, string msg) (message response) {
        message request;
        return response;
    }
}
