import ballerina.net.http;

service<http:Service> SampleService {

  resource sampleResource (http:ServerConnector conn, http:Request req) {
    reply;
  }
}
