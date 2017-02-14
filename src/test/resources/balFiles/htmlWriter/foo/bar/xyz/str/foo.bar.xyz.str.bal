package foo.bar.xyz.str;

@doc:Description("Test connector")
@doc:Param("consumerKey: consumer key")
@doc:Param("consumerSecret: consumer secret")
@doc:Param("accessToken: access token")
@doc:Param("accessTokenSecret: access token secret")
connector TestConnector(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    @doc:Description("test connector action")
    @doc:Param("t: a string argument")
    @doc:Param("msg: a string message")
    @doc:Return("response1: response object")
    action testAction1(TestConnector t, string msg) (message response1) {
        message request;
        message response;
        return response;
    }

    @doc:Description("test connector action2")
    @doc:Param("t: a string argument")
    @doc:Param("msg: a string message")
    @doc:Return("response2: response object")
    action testAction2(TestConnector t, string msg) (message response2) {
        message request;
        message response;
        return response;
    }

    @doc:Description("test connector action3")
    @doc:Param("t: a string argument")
    @doc:Param("msg: a string message")
    @doc:Return("response3: response object")
    action testAction3(TestConnector t, string msg) (message response3) {
        message request;
        message response;
        return response;
    }
}
