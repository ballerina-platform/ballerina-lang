@http:BasePath ("/var")
service echo {

    int int_value;

    @http:GET
    @http:Path ("/message")
    resource echo (message m) {
        int_value = 10;
        reply m;
    }
}
