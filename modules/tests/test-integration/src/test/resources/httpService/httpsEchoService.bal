import ballerina.net.http;

@http:config {
    basePath:"/echo",
    port:9095,
    scheme:"https",
    keyStoreFile:"${ballerina.home}/bre/security/wso2carbon.jks",
    keyStorePass:"wso2carbon",
    certPass:"wso2carbon"
}
service<http> echo {

    @http:POST{}
    @http:Path {value:"/"}
    resource echo (message m) {
        http:convertToResponse(m);
        reply m;

    }
}
