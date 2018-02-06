@http:BasePath {value:"/var"}
service echo {

    int int_value;

    @http:GET {}
    @http:Path {value:"/message"}
    resource echo (message m) {
        int_value = 10;
        reply m;
    }
}
