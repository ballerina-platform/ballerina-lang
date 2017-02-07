package a.b;

@Description("HelloWorld connector")
@Param("args: arguments")
connector HelloWorld(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    @Description("HelloWorld connector action")
    @Param("args: arguments")
    @Return("response object")
    action sayHello(string t, string msg) (message) {
        message request;
        message response;
        return response;
    }
}
