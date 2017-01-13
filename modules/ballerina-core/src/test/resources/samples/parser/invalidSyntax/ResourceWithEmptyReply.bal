package samples.parser.invalidSyntax;

import ballerina.lang.system;

@BasePath ("/samples")
service SampleService {

  @GET
  @Path ("/resource")
  resource sampleResource (message m) {
	reply;
  }
}
