import ballerina/lang.jsons;
import ballerina/lang.messages;
import ballerina/lang.strings;
import ballerina/lang.system;
import ballerina/net.http;
import ballerina/net.uri;
import ballerina/utils;

    connector    Twitter    ( string    consumerKey   ,   string    consumerSecret    ,    string    accessToken    ,    string     accessTokenSecret  )     {

         http    :   ClientConnector     tweeterEP   =    create  http    :   ClientConnector    (   "https://api.twitter.com"  ) ;

     action   tweet     (  Twitter   t ,     string  msg )         (      message   )  {
         message     request  =   { }    ;
             string  oauthHeader   =         constructOAuthHeader   (consumerKey,   consumerSecret,  accessToken,     accessTokenSecret,   msg  ) ;
        string tweetPath = "/1.1/statuses/update.json?status=" +    uri :   encode( msg);
        messages:setHeader(request, "Authorization", oauthHeader);
        message response = http:ClientConnector.post (tweeterEP, tweetPath, request);
        return response;
    }
}

function         constructOAuthHeader        (string consumerKey, string consumerSecret,        string accessToken,   string accessTokenSecret, string tweetMessage) (string) {
    string timeStamp = strings:valueOf(system:epochTime());
    string nonceString = utils:getRandomString();
    string paramStr = "oauth_consumer_key=" + consumerKey + "&oauth_nonce=" + nonceString + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp=" + timeStamp + "&oauth_token=" + accessToken + "&oauth_version=1.0&status=" + uri:encode(tweetMessage);
    string baseString =     "POST&" + uri:encode("https://api.twitter.com/1.1/statuses/update.json") + "&" + uri:encode(paramStr);
    string keyStr = uri:encode(consumerSecret) + "&" + uri:encode(accessTokenSecret);
        string signature =   utils  : getHmac(baseString    ,   keyStr, "SHA1"  )   ;
      string  oauthHeader =    "OAuth oauth_consumer_key=\"" + consumerKey + "\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"" + timeStamp + "\",oauth_nonce=\"" + nonceString + "\",oauth_version=\"1.0\",oauth_signature=\"" + uri:encode(signature) + "\",oauth_token=\"" + uri:encode(accessToken) + "\"";
    return strings: unescape(oauthHeader);
}

function     main    ( string[]     args    )    {
       Twitter  twitterConnector    =   create   Twitter(  args    [   0  ],   args[   1]  ,     args[2] ,    args[3   ]    )   ;
    message     tweetResponse = Twitter .    tweet  (   twitterConnector   ,   args [    4   ]  )   ;
      json    tweetJSONResponse =     messages   :  getJsonPayload(   tweetResponse  ) ;
    system  :   println  (jsons :   toString    (   tweetJSONResponse   )  )    ;
}
