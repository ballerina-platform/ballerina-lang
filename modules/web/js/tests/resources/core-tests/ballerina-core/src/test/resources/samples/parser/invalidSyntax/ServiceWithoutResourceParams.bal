package samples.parser;

service HelloService {

  @Path {value:"/tweet"}
  resource tweet {
      int b;
      reply m;
  }
}
