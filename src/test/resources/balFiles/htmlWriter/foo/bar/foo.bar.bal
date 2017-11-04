package foo.bar;

@Description{value:"Add HTTP header to the message"}
@Param{value:"m: Incoming message"}
@Param{value:"key: HTTP header key"}
@Param{value:"value: HTTP header value"}
public function addHeader (string m, string key, string value) {
    println("invoked");
}

@Description{value:"Get HTTP header from the message"}
@Param{value:"m: Incoming message"}
@Param{value:"key: HTTP header key"}
@Return{value:"HTTP header value"}
public function getHeader (string m, string key) (string) {
    println("invoked");
    return "value";
}

@Description{value:"an Argument"}
@Field{value:"text: a string"}
@Field{value:"argumentId: an id"}
@Field{value:"sentiment: setiment about the argument"}
public struct Argument {
    string text;
    int argumentId;
    int sentiment;
}

@Description{value:"Test connector"}
@Param{value:"consumerKey: consumer key"}
@Param{value:"consumerSecret: consumer secret"}
@Param{value:"accessToken: access token"}
@Param{value:"accessTokenSecret: access token secret"}
public connector TestConnector(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {

    @Description{value:"test connector action"}
    @Param{value:"msg: a string message"}
    @Return{value:"response: response object"}
    action testAction1(string msg) (string response) {
        string request;
        response = request;
        return response;
    }

    @Description{value:"test connector action2"}
    @Param{value:"msg: a string message"}
    @Return{value:"response2: response object"}
    action testAction2(string msg) (string response) {
        string request;
        return response;
    }
}
