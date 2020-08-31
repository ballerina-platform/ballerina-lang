import ballerina/java;

public function interopWithArrayAndMap() returns byte[] | error {
    map<int> mapVal = { "keyVal":8};
    return getArrayValueFromMap("keyVal", mapVal);
}

public function getArrayValueFromMap(string key, map<int> mapValue) returns byte[] = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

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

public function interopWithErrorReturn() returns string {
    error e = acceptStringOrErrorReturn("example error with given reason");
    return e.message();
}

public function acceptStringOrErrorReturn(string s) returns error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function interopWithUnionReturn() returns boolean | error {
    var a = acceptIntUnionReturn(1);
    if (!(a is int)) {
        return false;
    }
    var b = acceptIntUnionReturn(2);
    if (!(b is handle)) {
        return false;
    }
    var c = acceptIntUnionReturn(3);
    if (!(c is float)) {
        return false;
    }
    var d = acceptIntUnionReturn(-1);
    if (!(d is boolean)) {
        return false;
    }
    return true;
}

public function acceptIntUnionReturn(int s) returns int|string|float|boolean|handle = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

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

public function interopWithRecordReturn() returns boolean {
    string newVal = "new name";
    Employee p = {name:"name begin"};
    Employee a = acceptRecordAndRecordReturn(p, 45,  newVal);
    if (a.name != newVal) {
        return false;
    }
    if (p.name != newVal) {
        return false;
    }
    return true;
}

public function acceptRecordAndRecordReturn(Employee e, int a,  string newVal) returns Employee = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function interopWithAnyReturn() returns boolean {
    var a = acceptIntAnyReturn(1);
    if (!(a is int)) {
        return false;
    }
    var b = acceptIntAnyReturn(2);
    if (!(b is string)) {
        return false;
    }
    var c = acceptIntAnyReturn(3);
    if (!(c is float)) {
        return false;
    }
    var d = acceptIntAnyReturn(-1);
    if (!(d is boolean)) {
        return false;
    }
    return true;
}

public function acceptIntAnyReturn(int s) returns any = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
    name:"acceptIntAndUnionReturn"
} external;

public function interopWithAnydataReturn() returns boolean {
    var a = acceptIntAnydataReturn(1);
    if (!(a is int)) {
        return false;
    }
    var b = acceptIntAnydataReturn(2);
    if (!(b is string)) {
        return false;
    }
    var c = acceptIntAnydataReturn(3);
    if (!(c is float)) {
        return false;
    }
    var d = acceptIntAnydataReturn(-1);
    if (!(d is boolean)) {
        return false;
    }
    return true;
}

public function acceptIntAnydataReturn(int s) returns anydata= @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
    name:"acceptIntStringAndUnionReturn"
} external;

public function acceptIntReturnIntThrowsCheckedException(int a) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptRecordAndRecordReturnWhichThrowsCheckedException(Employee e, string newVal) returns Employee = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptIntUnionReturnWhichThrowsCheckedException(int s) returns int|string|float|boolean = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptRefTypesAndReturnMapWhichThrowsCheckedException(Person a, [int, string, Person] b,
                    int|string|Employee c, error d, any e, anydata f, Employee g) returns map<any> = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptStringErrorReturnWhichThrowsCheckedException(string s) = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function getArrayValueFromMapWhichThrowsCheckedException(string key, map<int> mapValue) returns int[] = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;
