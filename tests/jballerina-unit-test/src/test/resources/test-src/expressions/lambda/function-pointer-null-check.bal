function checkFunctionPointerNullEqual() returns (int){
   function (int, string) returns (float|error) f = getIt();
   if (f == ()) {
       return 1;
   } else {
       return 0;
   }
}

function checkFunctionPointerNullNotEqual() returns (int){
   function (int, string) returns (float|error) f = getIt();
   if (f != ()) {
       return 1;
   } else {
       return 0;
   }
}

function getIt () returns (function (int, string) returns (float|error)) {
    function (int, string) returns (float|error) f = test;
    return f;
}

function test (int x, string s) returns (float|error) {
    var y = check int.convert(s);
    float f = x * 1.0 * y;
    return f;
}
