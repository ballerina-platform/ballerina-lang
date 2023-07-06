function test1 () returns (string|error) {

    function (string,int) returns (string) fp = function (string a, int b) returns (string){
                                                     return a + b.toString();
                                                 };
    any aValue = fp;
    return test2(aValue);
}

function test2(any a) returns (string|error) {
   var fp2 = <function (string,int) returns (string)> a;

   return fp2("test", 1);
}
