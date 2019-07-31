
public function testFunction(string args) {
    int [] numbers = [1, 2, 3, 4];
    string[] fs = numbers.map(function(int value) returns string {
        return value.toHexString();
    }).filter(function (string value) returns boolean { 
        return value.length() > 1;
    }).
}