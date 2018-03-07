function checkFunctionPointerNullEqual()(int){
   function (int, string) returns (float) f = getIt();
   if (f == null) {
       return 1;
   } else {
       return 0;
   }
}

function checkFunctionPointerNullNotEqual()(int){
   function (int, string) returns (float) f = getIt();
   if (f != null) {
       return 1;
   } else {
       return 0;
   }
}

function getIt () (function (int, string) returns (float) f) {
    f = test;
    return;
}

function test (int x, string s) returns (float f) {
    var y, _ = <int>s;
    f = x * 1.0 * y;
    return;
}
