import ballerina.doc;

@doc:Description{value:"HelloWorld connector"}
@doc:Param{value:"args: arguments"}
connector HelloWorld(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    
    @doc:Description{value:"HelloWorld connector action"}
    @doc:Param{value:"args: arguments"}
    @doc:Return{value:"response string"}
    action sayHello(string msg) (string ) {
        string request;
        string response;
        return response;
    }
}
