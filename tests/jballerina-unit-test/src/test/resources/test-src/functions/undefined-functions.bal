string str = "This is a test string";

int index = str.index("T"); // Compile error
string s = str.add("extra string"); // Compiler error

function testFunction1() {
    [int, string, float, map<string>] tup = [10, "Foo", 12.34, {"k":"Bar"}];
    int result = 0;
    tup.forEach(function (string|int|map<string>|float x) {
        if (x is int) {
            result += 10;
        } else if (x is string) {
            result += x.length();
        } else if (x is float) {
            result += <int>x;
        } else {
            result += x["k"].length(); // Compile error
        }
    });

    map<string> addrMap = {
            line1: "No. 20",
            line2: "Palm Grove",
            city: "Colombo 03"
    };
    addrMap.delete("city"); // Compile error
}
