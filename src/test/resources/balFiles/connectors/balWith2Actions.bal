package a.b;

@Description("Test connector")
@Param("args: arguments")
connector TestConnector(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    @Description("test connector action")
    @Param("args: arguments")
    @Return("response object")
    action testAction1(TestConnector t, string msg) (message) {
        message request;
        message response;
        return response;
    }

    @Description("test connector action")
    @Param("args: arguments")
    @Return("response object")
    action testAction2(TestConnector t, string msg) (message) {
        message request;
        message response;
        return response;
    }
}