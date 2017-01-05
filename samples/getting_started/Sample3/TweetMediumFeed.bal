package org.wso2.ballerina.sample;

import ballerina.lang.message;
import ballerina.lang.string;
import ballerina.lang.system;
import ballerina.lang.xml;
import ballerina.net.http;
import ballerina.net.uri;
import ballerina.util;

@BasePath ("/tweet")
service TweetMediumFeed {

    @GET
    @Path("/feed")
    resource getMediumFeed  (message m) {

        http:HTTPConnector mediumEP = new http:HTTPConnector("https://medium.com", 100);
        http:HTTPConnector tweeterEP = new http:HTTPConnector("https://api.twitter.com", 100);

        message mediumResponse;
        xml feedXML;
        string title;

        string consumerKey;
        string consumerSecret;
        string accessToken;
        string accessTokenSecret;
        string oauthHeader;
        string tweetPath;
        message response;

        consumerKey = message:getHeader(m, "consumerKey");
        consumerSecret = message:getHeader(m, "consumerSecret");
        accessToken = message:getHeader(m, "accessToken");
        accessTokenSecret = message:getHeader(m, "accessTokenSecret");

        mediumResponse = http:HTTPConnector.get(mediumEP, "/feed/@wso2", m);
        feedXML = message:getXmlPayload(mediumResponse);
        title = xml:getString(feedXML, "/rss/channel/item[1]/title/text()");

        system:println(title);

        oauthHeader = constructOAuthHeader(consumerKey, consumerSecret, accessToken, accessTokenSecret, title);

        system:println(oauthHeader);

        message:setHeader(m, "Authorization", oauthHeader);

        tweetPath = "/1.1/statuses/update.json?status="+uri:encode(title);

        response = http:HTTPConnector.post(tweeterEP, tweetPath, m);

        reply response;
    }
}

function constructOAuthHeader(string consumerKey, string consumerSecret,
                string accessToken, string accessTokenSecret, string tweetMessage) (string) {

    string paramStr;
    string baseString;
    string keyStr;
    string signature;
    string oauthHeader;
    string timeStamp;
    string nonceString;

    timeStamp = string:valueOf(system:epochTime());
    nonceString =  "bbb"+timeStamp+"aaa";

    paramStr = "oauth_consumer_key=" + consumerKey + "&oauth_nonce=" + nonceString + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp="+timeStamp+"&oauth_token="+accessToken+"&oauth_version=1.0&status="+uri:encode(tweetMessage);

    baseString = "POST&" + uri:encode("https://api.twitter.com/1.1/statuses/update.json") + "&" + uri:encode(paramStr);

    keyStr = uri:encode(consumerSecret)+"&"+uri:encode(accessTokenSecret);

    signature = util:getHmac(baseString, keyStr, "SHA1");

    oauthHeader = "OAuth oauth_consumer_key=\"" + consumerKey + "\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"" + timeStamp +
                                      "\",oauth_nonce=\"" + nonceString + "\",oauth_version=\"1.0\",oauth_signature=\"" + uri:encode(signature) + "\",oauth_token=\"" + uri:encode(accessToken) + "\"";

    return string:unescape(oauthHeader);

}