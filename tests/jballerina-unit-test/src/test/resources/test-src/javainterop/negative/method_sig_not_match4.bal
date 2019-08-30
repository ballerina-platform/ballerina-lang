import ballerinax/java;

public type Employee record {
    string name = "";
};

public type Person object {
    int age = 9;
    public function __init(int age) {
        self.age = age;
    }
};

public function acceptRefTypesAndReturnMapWhichThrowsCheckedException(Person a, [int, string, Person] b,
                    int|string|Employee c, error d, any e, anydata f, Employee g) returns map<any> = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;
