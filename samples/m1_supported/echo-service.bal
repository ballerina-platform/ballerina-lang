package samples.echo;
import ballerina.net.http;

service EchoService{

  @POST
  @Path ("/*")
  resource echoResource (message m) {
    http:convertToResponse(m);
    reply m;
  }

}
