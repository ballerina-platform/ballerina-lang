package foo.bar.xyz.str;

@Description("Test connector")
@Param("consumerKey: consumer key")
@Param("consumerSecret: consumer secret")
@Param("accessToken: access token")
@Param("accessTokenSecret: access token secret")
connector TestConnector(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    @Description("test connector action")
    @Param("t: a string argument")
    @Param("msg: a string message")
    @Return("response: response object")
    action testAction1(string t, string msg) (message) {
        message request;
        message response;
        return response;
    }

    @Description("test connector action2")
    @Param("args: arguments")
    @Return("response object")
    action testAction2(string t, string msg) (message) {
        message request;
        message response;
        return response;
    }
}
