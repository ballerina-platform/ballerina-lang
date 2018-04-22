
import ballerina/lang.messages;
import ballerina/lang.system;
import ballerina/lang.errors;

function requestInterceptor (message msg) (boolean, message) {
    string username;
    string password;
    try {
        username = messages:getHeader(msg, "username");
        password = messages:getHeader(msg, "password");
        messages:removeHeader(msg, "username");
        messages:removeHeader(msg, "password");
        if (username == "admin" && password == "admin") {
            messages:setHeader(msg, "authcode", "admin");
            return true, msg;
        }
    }catch (errors:Error e){
        // ignore error and fail request.
    }
    message res = {};
    messages:setStringPayload(res, "invalid login " + username);
    system:println("invalid login " + username);
    return false, res;
}


function main (string... args) {
    system:println("this is simpleAuth interceptor");
}

