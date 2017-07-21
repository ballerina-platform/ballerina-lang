import ballerina.net.http;
import ballerina.lang.messages;

@http:configuration {
    basePath:"/echo",
    httpsPort:9095,
    keyStoreFile:"${ballerina.home}/bre/security/wso2carbon.jks",
    keyStorePass:"wso2carbon",
    certPass:"wso2carbon"
}
service<http> echo {

    @http:POST{}
    @http:Path {value:"/"}
    resource echo (message m) {
        message resp = {};
        messages:setStringPayload(resp, "hello world");
        reply resp;

    }
}

@http:configuration {
    basePath:"/echoOne",
    port:9094,
    httpsPort:9095,
    keyStoreFile:"${ballerina.home}/bre/security/wso2carbon.jks",
    keyStorePass:"wso2carbon",
    certPass:"wso2carbon"
}
service<http> echoOne {

    @http:POST{}
    @http:Path {value:"/abc"}
    resource echoAbc (message m) {
        message resp = {};
        messages:setStringPayload(resp, "hello world");
        reply resp;

    }
}


@http:configuration {
    basePath:"/echoDummy"
}
service<http> echoDummy {

    @http:POST{}
    @http:Path {value:"/"}
    resource echoDummy (message m) {
        message resp = {};
        messages:setStringPayload(resp, "hello world");
        reply resp;

    }

}
