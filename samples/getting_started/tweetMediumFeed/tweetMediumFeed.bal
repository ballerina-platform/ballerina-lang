import ballerina.lang.array;
import ballerina.lang.message;
import ballerina.lang.string;
import ballerina.lang.system;
import ballerina.lang.xml;
import ballerina.net.http;
import ballerina.net.uri;
import ballerina.util;

function main (string[] args) {

    http:HTTPConnector mediumEP = create http:HTTPConnector("https://medium.com");
    http:HTTPConnector tweeterEP = create http:HTTPConnector("https://api.twitter.com");

    int argumentLength = array:length(args);

    if (argumentLength < 4) {

        system:println("Incorrect number of arguments");
        system:println("Please specify: consumerKey consumerSecret accessToken accessTokenSecret");

    } else  {

        string consumerKey = args[0];
        string consumerSecret = args[1];
        string accessToken = args[2];
        string accessTokenSecret = args[3];

        message request = {};

        message mediumResponse = http:HTTPConnector.get(mediumEP, "/feed/@wso2", request);

        xml feedXML = message:getXmlPayload(mediumResponse);
        string title = xml:getString(feedXML, "/rss/channel/item[1]/title/text()");
        string oauthHeader = constructOAuthHeader(consumerKey, consumerSecret, accessToken, accessTokenSecret, title);
        message:setHeader(request, "Authorization", oauthHeader);
        string tweetPath = "/1.1/statuses/update.json?status="+uri:encode(title);

        message response = http:HTTPConnector.post(tweeterEP, tweetPath, request);

        system:println("Successfully tweeted: '" + title + "'");
    }

}

function constructOAuthHeader(string consumerKey, string consumerSecret,
                string accessToken, string accessTokenSecret, string tweetMessage) (string) {

    string timeStamp = string:valueOf(system:epochTime());
    string nonceString =  util:getRandomString();
    string paramStr = "oauth_consumer_key=" + consumerKey + "&oauth_nonce=" + nonceString + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp="+timeStamp+"&oauth_token="+accessToken+"&oauth_version=1.0&status="+uri:encode(tweetMessage);
    string baseString = "POST&" + uri:encode("https://api.twitter.com/1.1/statuses/update.json") + "&" + uri:encode(paramStr);
    string keyStr = uri:encode(consumerSecret)+"&"+uri:encode(accessTokenSecret);
    string signature = util:getHmac(baseString, keyStr, "SHA1");
    string oauthHeader = "OAuth oauth_consumer_key=\"" + consumerKey + "\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"" + timeStamp +
                                      "\",oauth_nonce=\"" + nonceString + "\",oauth_version=\"1.0\",oauth_signature=\"" + uri:encode(signature) + "\",oauth_token=\"" + uri:encode(accessToken) + "\"";

    return string:unescape(oauthHeader);
}
