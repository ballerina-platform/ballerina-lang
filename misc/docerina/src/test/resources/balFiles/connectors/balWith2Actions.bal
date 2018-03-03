@Description{value:"Test connector"}
@Param{value:"args: arguments"}
connector TestConnector(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    
    @Description{value:"test connector action"}
    @Param{value:"args: arguments"}
    @Return{value:"response object"}
    action testAction1(string msg) (int) {
        string request;
        int response;
        return response;
    }

    @Description{value:"test connector action"}
    @Param{value:"args: arguments"}
    @Return{value:"response object"}
    action testAction2(string msg) (string) {
        string request;
        string response;
        return response;
    }
}
