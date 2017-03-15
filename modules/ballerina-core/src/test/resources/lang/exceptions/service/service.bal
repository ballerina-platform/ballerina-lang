@http:BasePath ("/var")
service echo {

    @http:GET
    @http:Path ("/message")
    resource echo (message m) {
        int[] a = [11, 12, 13, 14];
        int value = a[10];
        reply m;
    }
}
