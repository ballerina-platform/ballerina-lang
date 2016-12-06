package samples.echo;

service EchoService{

  @POST
  @Path ("/*")
  resource echoResource (message m) {
      reply m;
  }

}
