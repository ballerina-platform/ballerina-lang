service helloWorld on listner1, a + b, c:d {

    function foo() {
    }

    resource function get elections/PE2020/Colombo(string elecName, string district, string div) {
    }
}

service foo on http:Listener(8080) {
  resource Text greeting = "Hello HTTP world";
}

service foo / on listner {
  resource Text greeting = "Hello HTTP world";

  resource function get .() {
  }
}

service foo /bar/baz on listner {
  resource Text greeting = "Hello HTTP world";

  resource function get x/y/z() {
      int a = 10;

      var serObj = service object {
          resource Text greeting = "Hello HTTP world";
      };
  }
}

service /bar/baz on listner {

}
