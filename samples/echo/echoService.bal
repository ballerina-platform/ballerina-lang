package samples.echo;

service EchoService{

  @GET
  @Path ("/*")
  resource echoResource (message m) {
      reply m;
  }

}
