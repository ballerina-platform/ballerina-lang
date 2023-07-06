import ballerina/jballerina.java;

function testPrintingObject() returns Person {
	Person p = new(20, "John", 2018, "March");
	println(p);
	return p;
}

class Person {
    public int age = 10;
    public string name = "sample name";

    int year = 50;
    string month = "February";

    function init (int age, string name, int year, string month) {
        self.age = age;
        self.name = name;
        self.year = year;
        self.month = month;
    }

    function toString() returns string {
        json result = {age: self.age, name: self.name};
        return result.toJsonString();
    }
}

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
