package a.b;

import ballerina.lang.message;
import ballerina.lang.system;

connector HelloWorld (string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    action sayHello(HelloWorld t, string msg) (message) {
        message request;
        message response;

        return response;
    }
}