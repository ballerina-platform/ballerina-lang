!function() {
  var foo = (function newFoo(result) {
    function foo() {
      console.log(result);
    }
    foo.result = function(result) {
      return newFoo(result);
    };
    return foo;
  })("fail");
  foo.result("success")();
}();
