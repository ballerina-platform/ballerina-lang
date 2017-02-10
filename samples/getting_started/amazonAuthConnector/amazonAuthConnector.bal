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

    AmazonAuthConnector amazonAuthConnector = create AmazonAuthConnector("AKIAIJ2IBQUCKKAL72IA", "AeUD3+Ic9BWH6ZEq+3K7zhxJ/zjzXkuicA883dPd", "us-east-1", "lambda", "aws4_request", "https://lambda.us-east-1.amazonaws.com");

    action invokeFunction(AmazonLambda amz, string arn) (message) throws exception {

        string signature;
        string httpMethod;
        string requestURI;
        string endpoint;
        message requestMsg;
        message response;

        httpMethod = "POST";
        requestURI = "/2015-03-31/functions/" + arn + "/invocations";
        endpoint = "https://lambda." + region + ".amazonaws.com";

        message:setHeader(requestMsg, "Host", endpoint);
        response = AmazonAuthConnector.req(amazonAuthConnector, requestMsg, httpMethod, requestURI, "");

        return response;
    }
    action deleteFunction(AmazonLambda amz, string arn) (message) throws exception {

            string signature;
            string httpMethod;
            string requestURI;
            string host;
            string endpoint;
            message requestMsg;
            message response;

            httpMethod = "DELETE";
            requestURI = "/2015-03-31/functions/" + arn;
            host = "lambda.us-east-1.amazonaws.com";
            endpoint = "https://lambda." + region + ".amazonaws.com";

            message:setHeader(requestMsg, "Host", host);
            response = AmazonAuthConnector.req(amazonAuthConnector, requestMsg, httpMethod, requestURI, "");

            return response;
    }
    action getAccountDetails(AmazonLambda amz) (message) throws exception {

            string signature;
            string httpMethod;
            string requestURI;
            string host;
            string endpoint;
            message requestMsg;
            message response;

            httpMethod = "GET";
            requestURI = "/2016-08-19/account-settings/";
            host = "lambda.us-east-1.amazonaws.com";
            endpoint = "https://lambda." + region + ".amazonaws.com";

            message:setHeader(requestMsg, "Host", host);
            response = AmazonAuthConnector.req(amazonAuthConnector, requestMsg, httpMethod, requestURI, "");

            return response;
    }
    action getFunction(AmazonLambda amz, string arn) (message) throws exception {

            string signature;
            string httpMethod;
            string requestURI;
            string host;
            string endpoint;
            message requestMsg;
            message response;

            httpMethod = "GET";
            requestURI = "/2015-03-31/functions/" + arn;
            host = "lambda.us-east-1.amazonaws.com";
            endpoint = "https://lambda." + region + ".amazonaws.com";

            message:setHeader(requestMsg, "Host", host);
            response = AmazonAuthConnector.req(amazonAuthConnector, requestMsg, httpMethod, requestURI, "");

            return response;
    }
    action listFunctions(AmazonLambda amz) (message) throws exception {

            string signature;
            string httpMethod;
            string requestURI;
            string host;
            string endpoint;
            message requestMsg;
            message response;

            httpMethod = "GET";
            requestURI = "/2015-03-31/functions/";
            host = "lambda.us-east-1.amazonaws.com";
            endpoint = "https://lambda." + region + ".amazonaws.com";

            message:setHeader(requestMsg, "Host", host);
            response = AmazonAuthConnector.req(amazonAuthConnector, requestMsg, httpMethod, requestURI, "");

            return response;
    }
    action getFunctionVersions(AmazonLambda amz, string arn) (message) throws exception {

            string signature;
            string httpMethod;
            string requestURI;
            string host;
            string endpoint;
            message requestMsg;
            message response;

            httpMethod = "GET";
            requestURI = "/2015-03-31/functions/" + arn + "/versions";
            host = "lambda.us-east-1.amazonaws.com";
            endpoint = "https://lambda." + region + ".amazonaws.com";

            message:setHeader(requestMsg, "Host", host);
            response = AmazonAuthConnector.req(amazonAuthConnector, requestMsg, httpMethod, requestURI, "");

            return response;
    }
}

connector AmazonAuthConnector(string accessKeyId, string secretAccessKey,
                string region, string serviceName, string terminationString, string endpoint) {


    http:HTTPConnector awsEP = new http:HTTPConnector(endpoint);

    action req(AmazonAuthConnector amz, message requestMsg, string httpMethod, string requestURI, string payload) (message) throws exception {

        message response;

        requestMsg = generateSignature(requestMsg, accessKeyId, secretAccessKey, region, serviceName, terminationString, httpMethod, requestURI, "");

        if(string:equalsIgnoreCase(httpMethod,"POST")){
            response = http:HTTPConnector.post(awsEP, requestURI, requestMsg);
        }else if(string:equalsIgnoreCase(httpMethod,"GET")){
            response = http:HTTPConnector.get(awsEP, requestURI, requestMsg);
        }else if(string:equalsIgnoreCase(httpMethod,"PUT")){
            response = http:HTTPConnector.put(awsEP, requestURI, requestMsg);
        }else if(string:equalsIgnoreCase(httpMethod,"DELETE")){
            response = http:HTTPConnector.delete(awsEP, requestURI, requestMsg);
        }

        return response;

    }

}


function main (string[] args) {

    AmazonLambda amzLamConnector = create AmazonLambda("AKIAIJ2IBQUCKKAL72IA", "AeUD3+Ic9BWH6ZEq+3K7zhxJ/zjzXkuicA883dPd", "us-east-1", "lambda", "aws4_request");
    message lambdaResponse;
    json lambdaJSONResponse;

    lambdaResponse = AmazonLambda.invokeFunction(amzLamConnector, "arn:aws:lambda:us-east-1:141896495686:function:testFunction");

    lambdaJSONResponse = message:getJsonPayload(lambdaResponse);
    system:println(json:toString(lambdaJSONResponse));

}



function generateSignature(message msg, string accessKeyId, string secretAccessKey, string region, string serviceName,
    string terminationString, string httpMethod, string requestURI, string payload) (message) throws exception {

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


    signingKey =  util:getHmacFromBase64( terminationString,util:getHmacFromBase64( serviceName,
    util:getHmacFromBase64( region,util:getHmacFromBase64(shortDate,util:base64encode("AWS4" + secretAccessKey),
    algorithm), algorithm), algorithm), algorithm);


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
    authHeader = authHeader + string:toLowerCase(util:getHmacBase16(stringToSign, signingKey, algorithm));

    message:setHeader(msg, "Authorization", authHeader);
    return msg;

}






