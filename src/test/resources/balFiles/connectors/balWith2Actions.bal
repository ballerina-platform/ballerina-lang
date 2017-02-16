@doc:Description("Test connector")
@doc:Param("args: arguments")
connector TestConnector(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    @doc:Description("test connector action")
    @doc:Param("args: arguments")
    @doc:Return("response object")
    action testAction1(TestConnector t, string msg) (message) {
        message request;
        message response;
        return response;
    }

    @doc:Description("test connector action")
    @doc:Param("args: arguments")
    @doc:Return("response object")
    action testAction2(TestConnector t, string msg) (message) {
        message request;
        message response;
        return response;
    }
}