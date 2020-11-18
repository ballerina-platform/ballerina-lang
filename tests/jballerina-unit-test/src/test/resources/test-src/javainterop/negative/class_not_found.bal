import ballerina/java;

public type Employee record {
    string name = "";
};

public class Person {
    int age = 9;
    public function init(int age) {
        self.age = age;
    }
}

public function interopWithRefTypesAndMapReturn() returns map<any> {
    Person a = new Person(44);
    [int, string, Person] b = [5, "hello", a];
    Employee c = {name:"sameera"};
    error d = error ("error reason");
    Person e = new Person(55);
    int f = 83;
    Employee g = {name:"sample"};
    return acceptRefTypesAndReturnMap(a, b, c, d, e, f, g);
}

public function acceptRefTypesAndReturnMap(Person a, [int, string, Person] b, int|string|Employee c, error d, any e, anydata f, Employee g) returns map<any> = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/PublicStaticMethods"
} external;
