import ballerina.doc;

@doc:Description{value:"Test connector"}
@doc:Param{value:"args: arguments"}
connector TestConnector(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    
    @doc:Description{value:"test connector action"}
    @doc:Param{value:"args: arguments"}
    @doc:Return{value:"response object"}
    action testAction1(string msg) (message) {
        message request;
        message response;
        return response;
    }

    @doc:Description{value:"test connector action"}
    @doc:Param{value:"args: arguments"}
    @doc:Return{value:"response object"}
    action testAction2(string msg) (message) {
        message request;
        message response;
        return response;
    }
}