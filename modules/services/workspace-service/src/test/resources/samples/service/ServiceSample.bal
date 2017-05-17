package test.samples;

@http:BasePath{ value: "/Hello"}
service HelloService {

  @http:BasePath{ value: "/Hello"}
  @http:GET{}
  resource tweet (message m) {
      reply m;
  }

}

function test(int a) (int) {
   return a+2;
}