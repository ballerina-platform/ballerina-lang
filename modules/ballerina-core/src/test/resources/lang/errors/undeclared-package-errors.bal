@BasePath ("/test")
service echo {
    @GET
    @Path ("/error")
    resource echo (message m) {
        testStackTrace();
        reply m;
    }
}

function testStackTrace() {
  string[] fruits;
  string apple;
  apple = getFruit1(fruits);
}

function getFruit1(string[] fruits) (string) {
  return getFruit2(fruits);
}

function getFruit2(string[] fruits) (string) {
  return getApple(fruits);
}

function getApple(string[] fruits) (string) {
  return fruits[24];
}
