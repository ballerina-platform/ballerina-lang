@doc:Description("HelloWorld connector")
@doc:Param("args: arguments")
connector HelloWorld(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    @Description("HelloWorld connector action")
    @Param("args: arguments")
    @Return("response object")
    action sayHello(HelloWorld t, string msg) (message) {
        message request;
        message response;
        return response;
    }
}
