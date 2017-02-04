package samples.deployment;

service EchoService{

  @POST
  @Path ("/*")
  resource echoResource (message m) {
      reply m;
  }

}
