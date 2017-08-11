package foo.bar;

import ballerina.doc;
import ballerina.lang.system;

@doc:Description{value:"Add HTTP header to the message"}
@doc:Param{value:"m: Incoming message"}
@doc:Param{value:"key: HTTP header key"}
@doc:Param{value:"value: HTTP header value"}
function addHeader (message m, string key, string value) {
    system:println("invoked");
}

@doc:Description{value:"Get HTTP header from the message"}
@doc:Param{value:"m: Incoming message"}
@doc:Param{value:"key: HTTP header key"}
@doc:Return{value:"HTTP header value"}
function getHeader (message m, string key) (string) {
    system:println("invoked");
    return "value";
}

@doc:Description{value:"an Argument"}
@doc:Field{value:"text: a string"}
@doc:Field{value:"argumentId: an id"}
@doc:Field{value:"sentiment: setiment about the argument"}
struct Argument {
    string text;
    int argumentId;
    int sentiment;
}

@doc:Description{value:"Test connector"}
@doc:Param{value:"consumerKey: consumer key"}
@doc:Param{value:"consumerSecret: consumer secret"}
@doc:Param{value:"accessToken: access token"}
@doc:Param{value:"accessTokenSecret: access token secret"}
connector TestConnector(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {

    @doc:Description{value:"test connector action"}
    @doc:Param{value:"msg: a string message"}
    @doc:Return{value:"response: response object"}
    action testAction1(string msg) (message response) {
        message request;
        response = request;
        return response;
    }

    @doc:Description{value:"test connector action2"}
    @doc:Param{value:"msg: a string message"}
    @doc:Return{value:"response2: response object"}
    action testAction2(string msg) (message response) {
        message request;
        return response;
    }
}
