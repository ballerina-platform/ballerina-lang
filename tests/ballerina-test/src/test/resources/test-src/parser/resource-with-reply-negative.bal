import ballerina.net.http;

service<http> SampleService {

  resource sampleResource (http:Connection conn, http:Request req) {
    reply;
  }
}
