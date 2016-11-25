package samples.echo;

Service EchoService{

@GET
@Path ("/*")
resource echoResource (message m) {
    reply m;
}

}
