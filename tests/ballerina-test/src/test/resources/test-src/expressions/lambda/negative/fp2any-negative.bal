function test1 () returns (string) {

    function (string,int) returns (string) fp = (string a, int b) => (string){
                                                     return a + b;
                                                 };
    any aValue = fp;
    return test2(aValue);
}

function test2(any a) returns (string){
   var fp2 =? (function (string,int) returns (string)) a;

   return fp2("test", 1);
}

