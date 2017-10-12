import ballerina.doc;

@doc:Description{value:"Test connector"}
@doc:Param{value:"args: arguments"}
connector TestConnector(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    
    @doc:Description{value:"test connector action"}
    @doc:Param{value:"args: arguments"}
    @doc:Return{value:"response object"}
    action testAction1(string msg) (int) {
        string request;
        int response;
        return response;
    }

    @doc:Description{value:"test connector action"}
    @doc:Param{value:"args: arguments"}
    @doc:Return{value:"response object"}
    action testAction2(string msg) (string) {
        string request;
        string response;
        return response;
    }
}