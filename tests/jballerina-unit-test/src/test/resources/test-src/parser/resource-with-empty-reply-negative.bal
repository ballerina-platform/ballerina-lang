import ballerina/lang.system;

@BasePath {value:"/samples"}
service<http> SampleService {

  @GET{}
  @Path {value:"/resource"}
  resource sampleResource (message m) {
    reply;
  }
}
