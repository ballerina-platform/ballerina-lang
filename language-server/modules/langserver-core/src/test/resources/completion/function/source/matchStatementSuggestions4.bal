import ballerina/http;
import ballerina/io;

function bar (string | int | boolean i)  returns (string | int | boolean) {
    string | int | boolean var1 =  "Test";
    (string, int, boolean) var2 = ("Foo", 12, true);
    (string, int)|(float, (string, boolean))|(float, (string, (boolean, int)))|float var3 = (66.6, ("SS", (true, 456)));
    int var4 = 12;
    string var5 = "test";
    Person p = { name : "Tom", age : 10};

    match http:

    return "";
}

type Person record {
    string name;
    int age;
};
