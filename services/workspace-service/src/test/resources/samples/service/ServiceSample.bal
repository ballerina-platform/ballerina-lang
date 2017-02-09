package test.samples;

service HelloService {

  @GET
  @Path ("/tweet")
  resource tweet (message m) {
      reply m;
  }

}

function test(int a) (int) {
   return a+2;
}