type Coordinates record {|
    decimal latitude;
    decimal longitude;
|};

function getString(string myStr) returns string {
    return "hello";
}

function myFunction(function () returns string func, string city, Coordinates coordinates) {
    int myInt = 10;
    string myStr = "myStr";
    getString()
}
