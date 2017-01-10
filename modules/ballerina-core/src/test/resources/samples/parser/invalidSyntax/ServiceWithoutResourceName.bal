package samples.parser;

service HelloService {

  @Path ("/tweet")
  // Following line is invalid.
  resource {
      int b;
      reply m;
  }
}
