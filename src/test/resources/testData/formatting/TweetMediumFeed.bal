import ballerina.lang.arrays;
import ballerina.lang.messages;
import ballerina.lang.strings;
import ballerina    .lang.  system;
import ballerina    .lang.xmls;
import   ballerina.net.http;
import ballerina.net.uri;
import   ballerina. utils;

        function        main    ( string   [   ]  args    )       {

        http    :   ClientConnector     tweeterEP   =    create     http    :   ClientConnector(   "https://api.twitter.com"  ) ;
    http:   ClientConnector     mediumEP =  create   http:ClientConnector(    "https://medium.com")   ;

      int     argumentLength =     arrays :length  ( args )    ;
    if   (    argumentLength  < 4  ) {
         system  : println( "Incorrect number of arguments"  );
        system: println ("Please specify: consumerKey consumerSecret accessToken accessTokenSecret");
    } else {
        string   consumerKey  =    args   [  0 ]    ;
               string consumerSecret = args[1];
        string accessToken = args[2];
           string accessTokenSecret = args[3];
        message request = {};
          message  mediumResponse =     http:  ClientConnector   .   get    (mediumEP,     "/feed/@wso2",  request )    ;
        xml feedXML = messages:getXmlPayload(mediumResponse);
        string title = xmls :    getString   (   feedXML,   "/rss/channel/item[1]/title/text()" );
        string oauthHeader =     constructOAuthHeader(consumerKey, consumerSecret, accessToken, accessTokenSecret, title);
        messages:   setHeader( request, "Authorization",    oauthHeader);
        string tweetPath =   "/1.1/statuses/update.json?status="    + uri:encode(title);
        message response =  http:    ClientConnector.post (tweeterEP, tweetPath, request);
         system  : println  (   "Successfully tweeted: '"     +  title     +   "'"  );
    }
}

function    constructOAuthHeader    ( string    consumerKey   ,   string   consumerSecret,    string     accessToken,   string    accessTokenSecret,  string  tweetMessage )     (  string    )   {
     string   timeStamp     =  strings: valueOf ( system:  epochTime())  ;
    string nonceString = utils:getRandomString();
    string paramStr = "oauth_consumer_key=" + consumerKey + "&oauth_nonce=" + nonceString + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp=" + timeStamp + "&oauth_token=" + accessToken + "&oauth_version=1.0&status=" + uri:encode(tweetMessage);
    string baseString = "POST&" + uri:encode("https://api.twitter.com/1.1/statuses/update.json") + "&" + uri:encode(paramStr);
    string keyStr = uri:encode(consumerSecret) + "&" + uri:encode(accessTokenSecret);
      string  signature =  utils:   getHmac(   baseString,    keyStr,     "SHA1" );
          string  oauthHeader  =    "OAuth oauth_consumer_key=\""  +     consumerKey + "\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\""     +    timeStamp  +    "\",oauth_nonce=\"" + nonceString + "\",oauth_version=\"1.0\",oauth_signature=\""  +    uri:encode(signature) +    "\",oauth_token=\"" +  uri:encode(accessToken)  +    "\""  ;
    return  strings      : unescape(    oauthHeader )    ;
}
