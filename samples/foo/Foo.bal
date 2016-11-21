function foo () {
  HttpEndpoint e1 = new HttpEndpoint ("http://localhost:1111");
  int handle = e1.sendGet ("/foo");

  HttpEndpoint e1 =  new ballerina.net.http.endpoint ("http://localhost:2222");
  actor a1 with endpoint e1;
  int handle = ballerina.net.http.sendGet (e1, "/foo");


  actor HttpEndpoint e1 = new ballerina.net.http.endpoint ("http://localhost:2222");
  int handle = ballerina.net.http.sendGet (e1, "/foo");

  var HttpEndpoint e2 = new ballerina.net.http.endpoint ("http://localhost:2222");




  lifeline a1 (new ballerina.net.http.endpoint ("xxx"));
  int handle = ballerina.net.http.sendGet (a1, "/foo");

  define connector {
    get()
  }
  worker foo (Message m) {
    statement;
  }

  connector e1 = createconnector (ballerina.net.http.SOMETHING ("http://localhost:1111"));
  int handle = e1.sendGet ("/foo");

}
