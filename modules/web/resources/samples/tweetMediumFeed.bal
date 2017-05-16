import ballerina.lang.arrays;
import ballerina.lang.messages;
import ballerina.lang.strings;
import ballerina.lang.system;
import ballerina.lang.xmls;
import ballerina.net.http;
import ballerina.net.uri;
import ballerina.utils;

function main() {
    http:ClientConnector tweeterEP = create http:ClientConnector("https://api.twitter.com");
    http:ClientConnector mediumEP = create http:ClientConnector("https://medium.com");
    int argumentLength = arrays:length(undefined);

    if (undefined < 4) {
        system:println("Incorrect number of arguments");
        system:println("Please specify: consumerKey consumerSecret accessToken accessTokenSecret");
    } else {
        string consumerKey = args[0];
        string consumerSecret = args[1];
        string accessToken = args[2];
        string accessTokenSecret = args[3];
        message request = {};
        message mediumResponse = http:ClientConnector.get("/feed/@wso2", undefined);
        xml feedXML = messages:getXmlPayload(undefined);
        string title = xmls:getString(undefined, "/rss/channel/item[1]/title/text()");
        string oauthHeader = constructOAuthHeader(undefined, undefined, undefined, undefined, undefined);
        messages:setHeader(undefined, "Authorization", undefined);
        string tweetPath = "/1.1/statuses/update.json?status=" + uri:encode(undefined);
        message response = http:ClientConnector.post(undefined, undefined);
        system:println("Successfully tweeted: '" + undefined + "'");
    }
}

function constructOAuthHeader() {
    string timeStamp = strings:valueOf(system:epochTime());
    string nonceString = utils:getRandomString();
    string paramStr = "oauth_consumer_key=" + undefined + "&oauth_nonce=" + undefined + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp=" + undefined + "&oauth_token=" + undefined + "&oauth_version=1.0&status=" + uri:encode(undefined);
    string baseString = "POST&" + uri:encode("https://api.twitter.com/1.1/statuses/update.json") + "&" + uri:encode(undefined);
    string keyStr = uri:encode(undefined) + "&" + uri:encode(undefined);
    string signature = utils:getHmac(undefined, undefined, "SHA1");
    string oauthHeader = "OAuth oauth_consumer_key=\"" + undefined + "\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"" + undefined + "\",oauth_nonce=\"" + undefined + "\",oauth_version=\"1.0\",oauth_signature=\"" + uri:encode(undefined) + "\",oauth_token=\"" + uri:encode(undefined) + "\"";


    return strings:unescape(undefined);
}
