import ballerina/http;

service HelloService on new http:MockListener(9090) {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function tweet {
        int b;
    }
}
