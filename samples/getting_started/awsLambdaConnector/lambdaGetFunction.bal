package samples.connectors.sample;

import ballerina.lang.system;
import ballerina.lang.json;
import ballerina.lang.message;
import ballerina.lang.string;
import ballerina.net.http;
import ballerina.net.uri;
import ballerina.util;



connector AmazonLambda(string accessKeyId, string secretAccessKey,
                string region, string serviceName, string terminationString) {

    http:HTTPConnector lambdaEP = new http:HTTPConnector("https://lambda.us-east-1.amazonaws.com");

    action req(AmazonLambda amz, string arn) (message) throws exception {

        string signature;
        string httpMethod;
        string requestURI;
        string host;
        message requestMsg;
        message response;

        httpMethod = "GET";
        requestURI = "/2015-03-31/functions/" + arn;
        host = "lambda.us-east-1.amazonaws.com";


        message:setHeader(requestMsg, "Host", host);

        requestMsg = generateSignature(requestMsg, accessKeyId, secretAccessKey, region, serviceName, terminationString, httpMethod, requestURI, "");

        response = http:HTTPConnector.get(lambdaEP, requestURI, requestMsg);
        return response;

    }

}




function main (string[] args) {

    sample:AmazonLambda amzLamConnector = new sample:AmazonLambda("AKIAIJ2IBQUCKKAL72IA", "AeUD3+Ic9BWH6ZEq+3K7zhxJ/zjzXkuicA883dPd", "us-east-1", "lambda", "aws4_request");

    message lambdaResponse;
    json lambdaJSONResponse;

    lambdaResponse = sample:AmazonLambda.req(amzLamConnector, "arn:aws:lambda:us-east-1:141896495686:function:testFunction");

    lambdaJSONResponse = message:getJsonPayload(lambdaResponse);
    system:println(json:toString(lambdaJSONResponse));

}



function generateSignature(message msg, string accessKeyId, string secretAccessKey, string region, string serviceName, string terminationString, string httpMethod, string requestURI, string payload) (message) throws exception {

    string canonicalRequest;
    string canonicalQueryString;
    string stringToSign;
    string payloadBuilder;
    string payloadStrBuilder;
    string authHeader;
    string algorithm;

    string amzDate;
    string shortDate;


    string signedHeader;
    string canonicalHeaders;

    string signedHeaders;
    string requestPayload;

    string signingKey;

    algorithm = "SHA256";



    amzDate = system:getDateFormat("yyyyMMdd'T'HHmmss'Z'");
    shortDate = system:getDateFormat("yyyyMMdd");

    message:setHeader(msg, "X-Amz-Date", amzDate);

    canonicalRequest = httpMethod;
    canonicalRequest = canonicalRequest + "\n";

    canonicalRequest = canonicalRequest + string:replaceAll(uri:encode(requestURI), "%2F", "/");
    canonicalRequest = canonicalRequest + "\n";

    canonicalQueryString = "";

    canonicalRequest = canonicalRequest + canonicalQueryString;
    canonicalRequest = canonicalRequest + "\n";

    if(payload != ""){

            canonicalHeaders = canonicalHeaders + string:toLowerCase("Content-Type");
            canonicalHeaders = canonicalHeaders + ":";
            canonicalHeaders = canonicalHeaders + (message:getHeader(msg, string:toLowerCase("Content-Type")));
            canonicalHeaders = canonicalHeaders + "\n";
            signedHeader = signedHeader + string:toLowerCase("Content-Type");
            signedHeader = signedHeader + ";";
    }

    canonicalHeaders = canonicalHeaders + string:toLowerCase("Host");
    canonicalHeaders = canonicalHeaders + ":";
    canonicalHeaders = canonicalHeaders + message:getHeader(msg, string:toLowerCase("Host"));
    canonicalHeaders = canonicalHeaders + "\n";
    signedHeader = signedHeader + string:toLowerCase("Host");
    signedHeader = signedHeader + ";";

    canonicalHeaders = canonicalHeaders + string:toLowerCase("X-Amz-Date");
    canonicalHeaders = canonicalHeaders + ":";
    canonicalHeaders = canonicalHeaders + (message:getHeader(msg, string:toLowerCase("X-Amz-Date")));
    canonicalHeaders = canonicalHeaders + "\n";
    signedHeader = signedHeader + string:toLowerCase("X-Amz-Date");
    signedHeader = signedHeader;



    canonicalRequest = canonicalRequest + canonicalHeaders;
    canonicalRequest = canonicalRequest + "\n";


    signedHeaders = "";
    signedHeaders = signedHeader;


    canonicalRequest = canonicalRequest + signedHeaders;
    canonicalRequest = canonicalRequest + "\n";



    payloadBuilder = payload;

    requestPayload = "";
    requestPayload = payloadBuilder;


    canonicalRequest = canonicalRequest + string:toLowerCase(util:getHash(requestPayload, algorithm));


    //Start creating the string to sign

    stringToSign = stringToSign + "AWS4-HMAC-SHA256";
    stringToSign = stringToSign + "\n";
    stringToSign = stringToSign + amzDate;
    stringToSign = stringToSign + "\n";

    stringToSign = stringToSign + shortDate;
    stringToSign = stringToSign + "/";
    stringToSign = stringToSign + region;
    stringToSign = stringToSign + "/";
    stringToSign = stringToSign + serviceName;
    stringToSign = stringToSign + "/";
    stringToSign = stringToSign + terminationString;

    stringToSign = stringToSign + "\n";
    stringToSign = stringToSign + string:toLowerCase(util:getHash(canonicalRequest, algorithm));


    signingKey =  util:getHmacFromBase64( terminationString,util:getHmacFromBase64( serviceName,util:getHmacFromBase64( region,util:getHmacFromBase64(shortDate,util:base64encode("AWS4" + secretAccessKey), algorithm), algorithm), algorithm), algorithm);


    authHeader = authHeader + ("AWS4-HMAC-SHA256");
    authHeader = authHeader + (" ");
    authHeader = authHeader + ("Credential");
    authHeader = authHeader + ("=");
    authHeader = authHeader + (accessKeyId);
    authHeader = authHeader + ("/");
    authHeader = authHeader + (shortDate);
    authHeader = authHeader + ("/");
    authHeader = authHeader + (region);
    authHeader = authHeader + ("/");
    authHeader = authHeader + (serviceName);
    authHeader = authHeader + ("/");
    authHeader = authHeader + (terminationString);
    authHeader = authHeader + (",");
    authHeader = authHeader + (" SignedHeaders");
    authHeader = authHeader + ("=");
    authHeader = authHeader + (signedHeaders);
    authHeader = authHeader + (",");
    authHeader = authHeader + (" Signature");
    authHeader = authHeader + ("=");
    authHeader = authHeader + string:toLowerCase(util:getHmacFromBase64Base16(stringToSign, signingKey, algorithm));

    message:setHeader(msg, "Authorization", authHeader);
    return msg;

}