import module1;

function getStringInPkg() returns (string) {
    int a = 12;
    
    function (int , int ) returns (        ) sumFunction = function (int a, int b) returns (string) {
                                                            int value =  a + b;
                                                            return "sum is " + value;
                                                          };
    
    int b = 12;
}
