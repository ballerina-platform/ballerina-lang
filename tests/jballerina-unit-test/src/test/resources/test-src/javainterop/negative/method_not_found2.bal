import ballerina/java;

public class Person {
    int age = 9;
    public function init(int age) {
        self.age = age;
    }
}

public function interopWithObjectReturn() returns boolean {
    Person p = new Person(8);
    Person a = acceptObjectAndObjectReturn(p, 45, 4.5);
    if (a.age != 45) {
        return false;
    }
    if (p.age != 45) {
        return false;
    }
    return true;
}

public function acceptObjectAndObjectReturn(Person p, int newVal, float f) returns Person = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;
