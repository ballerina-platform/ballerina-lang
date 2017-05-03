package test.samples;

@http:BasePath{ value: "/Hello"}
service HelloService {

  @http:BasePath{ value: "/Hello"}
  @http:GET{}
  @http:Test{ key1 : "value1",
              key2 : @http:Bar{value: "value2"},
              key3 : [1, 2, 3, 4.5, 5],
              key4 : ["a", "b", "c"]}
  resource tweet (message m) {
      reply m;
  }

}

function test(int a) (int) {
   return a+2;
}