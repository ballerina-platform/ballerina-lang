function checkFunctionPointerNullEqual() returns (int){
   function (int, string) returns (float) f = getIt();
   if (f == null) {
       return 1;
   } else {
       return 0;
   }
}

function checkFunctionPointerNullNotEqual() returns (int){
   function (int, string) returns (float) f = getIt();
   if (f != null) {
       return 1;
   } else {
       return 0;
   }
}

function getIt () returns (function (int, string) returns (float)) {
    function (int, string) returns (float) f = test;
    return f;
}

function test (int x, string s) returns (float) {
    var y = check <int>s;
    float f = x * 1.0 * y;
    return f;
}
