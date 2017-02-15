package samples.parser;

service HelloService {

  @Path ("/tweet")
  // Following line is invalid.
  resource tweet {
      int b;
      reply m;
  }
}
