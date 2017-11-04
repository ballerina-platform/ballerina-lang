package foo.bar.xyz.str;

@Description{value:"Test connector"}
@Param{value:"consumerKey: consumer key"}
@Param{value:"consumerSecret: consumer secret"}
@Param{value:"accessToken: access token"}
@Param{value:"accessTokenSecret: access token secret"}
public connector TestConnector(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    
    @Description{value:"test connector action"}
    @Param{value:"msg: a string message"}
    @Return{value:"response1: response object"}
    action testAction1(string msg) (string response1) {
        string request;
        string response;
        return response;
    }

    @Description{value:"test connector action2"}
    @Param{value:"msg: a string message"}
    @Return{value:"response2: response object"}
    action testAction2(string msg) (string response2) {
        string request;
        string response;
        return response;
    }

    @Description{value:"test connector action3"}
    @Param{value:"msg: a string message"}
    @Return{value:"response3: response object"}
    action testAction3(string msg) (string response3) {
        string request;
        string response;
        return response;
    }
}
