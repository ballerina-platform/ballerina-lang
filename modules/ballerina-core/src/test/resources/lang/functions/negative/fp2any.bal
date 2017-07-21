function test1 () (string) {

    function (string,int)returns(string) fp = function (string a, int b) returns (string){
                                                     return a + b;
                                                 };
    any aValue = fp;
    return test2(aValue);
}

function test2(any a)(string){
   var fp2, error = (function (string,int) returns (string)) a;
   if(error != null){
        return error.msg;
   }
   return fp2("test", 1);
}

