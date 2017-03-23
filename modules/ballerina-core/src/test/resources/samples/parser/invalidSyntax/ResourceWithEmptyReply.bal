package samples.parser.invalidSyntax;

import ballerina.lang.system;

@BasePath {value:"/samples"}
service SampleService {

  @GET{}
  @Path {value:"/resource"}
  resource sampleResource (message m) {
	reply;
  }
}
