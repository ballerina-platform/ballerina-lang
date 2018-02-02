import ballerina.lang.arrays;
import ballerina.lang.jsons;
import ballerina.lang.messages;
import ballerina.lang.strings;
import ballerina.lang.system;
import ballerina.net.http;
import ballerina.net.uri;
import ballerina.utils;
function main(string[] args) {
    http:ClientConnector tweeterEP = create http:ClientConnector("https://api.twitter.com");
    http:ClientConnector gitHubEP = create http:ClientConnector("https://api.github.com");
    int argumentLength = arrays:length(args);
    if (argumentLength < 4) {
        system:println("Incorrect number of arguments");
        system:println("Please specify: consumerKey consumerSecret accessToken accessTokenSecret [repo-name]");
        
    }
    else {
        string consumerKey = args[0];
        string consumerSecret = args[1];
        string accessToken = args[2];
        string accessTokenSecret = args[3];
        string repo;
        if (argumentLength >= 5) {
            repo = args[4];
        
        }
        else {
            repo = "wso2-synapse";
            
        }
        string repoPRpath = "/repos/wso2/" + repo + "/pulls";
        message request = {};
        message gitHubResponse = http:ClientConnector.get(gitHubEP, repoPRpath, request);
        json gitHubJsonResponse = messages:getJsonPayload(gitHubResponse);
        int noOfPRs = jsons:getInt(gitHubJsonResponse, "$.length()");
        string noOfPRstr = strings:valueOf(noOfPRs);
        string textMsg = "Number of pending pull requests in " + repo + " is " + noOfPRstr;
        string oauthHeader = constructOAuthHeader(consumerKey, consumerSecret, accessToken, accessTokenSecret, textMsg);
        messages:setHeader(request, "Authorization", oauthHeader);
        string tweetPath = "/1.1/statuses/update.json?status=" + uri:encode(textMsg);
        message response = http:ClientConnector.post(tweeterEP, tweetPath, request);
        system:println("Successfully tweeted: '" + textMsg + "'");
        
    }
    
}
function constructOAuthHeader(string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret, string tweetMessage)(string ) {
    string timeStamp = strings:valueOf(system:epochTime());
    string nonceString = utils:getRandomString();
    string paramStr = "oauth_consumer_key=" + consumerKey + "&oauth_nonce=" + nonceString + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp=" + timeStamp + "&oauth_token=" + accessToken + "&oauth_version=1.0&status=" + uri:encode(tweetMessage);
    string baseString = "POST&" + uri:encode("https://api.twitter.com/1.1/statuses/update.json") + "&" + uri:encode(paramStr);
    string keyStr = uri:encode(consumerSecret) + "&" + uri:encode(accessTokenSecret);
    string signature = utils:getHmac(baseString, keyStr, "SHA1");
    string oauthHeader = "OAuth oauth_consumer_key=\"" + consumerKey + "\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"" + timeStamp + "\",oauth_nonce=\"" + nonceString + "\",oauth_version=\"1.0\",oauth_signature=\"" + uri:encode(signature) + "\",oauth_token=\"" + uri:encode(accessToken) + "\"";
    return strings:unescape(oauthHeader);
    
}
