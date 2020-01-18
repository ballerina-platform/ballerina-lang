import ballerinax/java;

public function interopWithArrayAndMap() returns int[] {
    map<int> mapVal = { "keyVal":8};
    return getArrayValueFromMap(java:fromString("keyVal"), mapVal);
}

public function getArrayValueFromMap(handle key, map<int> mapValue) returns int[] = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public type Employee record {
    string name = "";
};

public type Person object {
    int age = 9;
    public function __init(int age) {
        self.age = age;
    }
};

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
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function interopWithErrorReturn() returns string {
    error e = acceptStringErrorReturn(java:fromString("example error with given reason"));
    return e.reason();
}

public function acceptStringErrorReturn(handle s) returns error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

service testService = service {
    resource function onMessage(string text) {
        string a = text;
    }
};

public function acceptServiceAndBooleanReturn() returns boolean {
    return acceptServiceObjectAndReturnBoolean(testService);
}

public function acceptServiceObjectAndReturnBoolean(service s) returns boolean = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.StaticMethods"
} external;

public function interopWithUnionReturn() returns boolean {
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
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function interopWithObjectReturn() returns boolean {
    Person p = new Person(8);
    Person a = acceptObjectAndObjectReturn(p, 45);
    if (a.age != 45) {
        return false;
    }
    if (p.age != 45) {
        return false;
    }
    return true;
}

public function acceptObjectAndObjectReturn(Person p, int newVal) returns Person = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function interopWithRecordReturn() returns boolean {
    string newVal = "new name";
    Employee p = {name:"name begin"};
    Employee a = acceptRecordAndRecordReturn(p, java:fromString(newVal));
    if (a.name != newVal) {
        return false;
    }
    if (p.name != newVal) {
        return false;
    }
    return true;
}

public function acceptRecordAndRecordReturn(Employee e, handle newVal) returns Employee = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function interopWithAnyReturn() returns boolean {
    var a = acceptIntAnyReturn(1);
    if (!(a is int)) {
        return false;
    }
    var b = acceptIntAnyReturn(2);
    if (!(b is handle)) {
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

public function acceptIntAnyReturn(int s) returns any= @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
    name:"acceptIntUnionReturn"
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
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
    name:"acceptIntUnionReturn"
} external;

public function acceptIntReturnIntThrowsCheckedException(int a) returns int | error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptRecordAndRecordReturnWhichThrowsCheckedException(Employee e, handle newVal) returns Employee | error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptIntUnionReturnWhichThrowsCheckedException(int s) returns int|string|float|boolean|error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptRefTypesAndReturnMapWhichThrowsCheckedException(Person a, [int, string, Person] b,
                    int|string|Employee c, error d, any e, anydata f, Employee g) returns map<any> | error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptStringErrorReturnWhichThrowsCheckedException(handle s) returns error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function getArrayValueFromMapWhichThrowsCheckedException(handle key, map<int> mapValue) returns int[] | error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;


// JSON interop

function testJsonReturns() returns [json, json, json, json, json] {
	return [getJson(), getInt(), getJsonObject(), getJsonArray(), getNullJson()];
}

function testJsonParams() returns json {
	return getIntFromJson(7);
}

public function getJson() returns json = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function getInt() returns json = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function getJsonObject() returns json = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function getJsonArray() returns json = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getIntFromJson(json j) returns int = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function getNullJson() returns json = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

// XML interop

function testPassingXML() returns string {
    return <string> java:toString(getStringFromXML(xml `<foo/>`));
}

function getXML() returns xml = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

function getStringFromXML(xml x) returns handle = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;


// Finite type interop

type ALL_INT 1|2|3|4|5;
type MIX_TYPE 1 | 2 | 5 | "hello" | true | false;

function testAcceptAllInts() returns [int, float, int] {
    ALL_INT i = 4;
    return [acceptAllInts(i), acceptAllFloats(i), <int> acceptAny(i)];
}

function testAcceptMixTypes() returns [any, any, any] {
    ALL_INT i = 4;
    return [acceptMixType(2), acceptMixType("hello"), acceptMixType(false)];
}

function getAllInts() returns ALL_INT = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

function acceptAllInts(ALL_INT x) returns int = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

function acceptAllFloats(ALL_INT x) returns float = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

function acceptAny(ALL_INT x) returns any = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

function getMixType() returns MIX_TYPE = @java:Method {
    name:"getAny",
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

function getIntegersAsMixType() returns MIX_TYPE = @java:Method {
    name:"getAllInts",
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

function acceptMixType(MIX_TYPE x) returns any = @java:Method {
    name:"acceptAny",
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

function getInvalidIntegerAsMixType() returns MIX_TYPE = @java:Method {
    name:"getInvalidMixType",
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

// Function pointers with interop

function testUseFunctionPointer() returns int {
    return useFunctionPointer(function (int a, int b) returns int { return a + b; } );
}

function testGetFunctionPointer() returns int {
    var fp = getFunctionPointer(function (int a, int b) returns int { return a + b; } );
    return fp(4, 6);
}

function useFunctionPointer((function (int a, int b) returns int) fp) returns int = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

function getFunctionPointer(any x) returns (function (int a, int b) returns int) = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;


// Type desc with interop

function testUseTypeDesc() returns handle {
    return useTypeDesc(json);
}

function testGetTypeDesc() returns typedesc<any> {
    typedesc<any> td = getTypeDesc();
    return td;
}

function useTypeDesc(typedesc<any> t) returns handle = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

function getTypeDesc() returns typedesc<any> = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;


// future with interop

function testUseFuture() returns any {
    future<any> f = start getInt();
    _ = wait f;
    return useFuture(f);
}

function testGetFuture() returns any {
    future<any> f1 = start getInt();
    future<any> f2 = getFuture(f1);
    return wait f2;
}

function useFuture(future<any> f) returns any = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

function getFuture(any a) returns future<any> = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;


// handle type with interop

function testGetHandle() returns handle {
    return getHandle();
}

function testUseHandle() returns string {
    handle h = getHandle();
    return useHandle(h);
}

function useHandle(handle h) returns string = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

function getHandle() returns handle = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

function testUseHandleInUnion() returns string {
    handle|error h = getHandleOrError();
    if (h is handle) {
        return useHandle(h);
    } else {
        return "hello";
    }
}

function getHandleOrError() returns handle|error = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests",
    name: "getHandle"
} external;

public function testThrowJavaException2() returns any|error {
    handle javaStack = createJavaStack();
    return trap javaStackPop(javaStack);
}

function createJavaStack() returns handle = @java:Constructor {
    class: "java/util/Stack",
    paramTypes: []
} external;

function javaStackPop(handle stack) returns any = @java:Method {
    class: "java/util/Stack",
    name: "pop"
} external;
