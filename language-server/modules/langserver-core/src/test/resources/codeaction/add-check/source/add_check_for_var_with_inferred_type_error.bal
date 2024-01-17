var a = testFunction();

function testFunction() returns string|error {
   return "";
}

function testLocalVar() {
   var b = testFunction();
}

client class MyClass {
   remote function remote1() {
      var c = testFunction();
   }
}
