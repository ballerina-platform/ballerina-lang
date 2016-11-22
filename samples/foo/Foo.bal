function foo () {


  actor HttpEndpoint e1 = new ballerina.net.http.endpoint ("http://localhost:2222");
  int handle = ballerina.net.http.sendGet (e1, "/foo");

  var HttpEndpoint e2 = new ballerina.net.http.endpoint ("http://localhost:2222");

}
