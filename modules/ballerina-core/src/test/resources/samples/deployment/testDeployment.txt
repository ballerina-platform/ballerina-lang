package samples.deployment;

service EchoService{

  @POST
  @Path ("/*")
  resource echoResource (message m) {
      ballerina.lang.system:log(3, "Echo Invoked.");
      reply m;
  }

}
