@Description{value:"HelloWorld connector"}
@Param{value:"args: arguments"}
connector HelloWorld(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    
    @Description{value:"HelloWorld connector action"}
    @Param{value:"args: arguments"}
    @Return{value:"response string"}
    action sayHello(string msg) (string ) {
        string request;
        string response;
        return response;
    }
}
