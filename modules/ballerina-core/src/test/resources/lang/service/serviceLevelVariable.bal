@BasePath ("/var")
service echo {

    int int_value;

    @GET
    @Path ("/message")
    resource echo (message m) {
        int_value = 10;
        reply m;
    }
}
