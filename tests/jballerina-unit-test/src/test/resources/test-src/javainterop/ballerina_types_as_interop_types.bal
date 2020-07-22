import ballerina/java;

public function interopWithArrayAndMap() returns int[] {
    map<int> mapVal = { "keyVal":8};
    return getArrayValueFromMap("keyVal", mapVal);
}

public function getArrayValueFromMap(string key, map<int> mapValue) returns int[] = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public type Employee record {
    string name = "";
};

public type Person object {
    int age = 9;
    public function init(int age) {
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
    error e = acceptStringErrorReturn("example error with given reason");
    return e.message();
}

public function acceptStringErrorReturn(string s) returns error = @java:Method {
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
    Employee a = acceptRecordAndRecordReturn(p, newVal);
    if (a.name != newVal) {
        return false;
    }
    if (p.name != newVal) {
        return false;
    }
    return true;
}

public function acceptRecordAndRecordReturn(Employee e, string newVal) returns Employee = @java:Method {
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

public function acceptRecordAndRecordReturnWhichThrowsCheckedException(Employee e, string newVal) returns Employee | error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptIntUnionReturnWhichThrowsCheckedException(int s) returns int|string|float|boolean|error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptRefTypesAndReturnMapWhichThrowsCheckedException(Person a, [int, string, Person] b,
                    int|string|Employee c, error d, any e, anydata f, Employee g) returns map<any> | error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptStringErrorReturnWhichThrowsCheckedException(string s) returns error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function getArrayValueFromMapWhichThrowsCheckedException(string key, map<int> mapValue) returns int[] | error = @java:Method {
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
    return getStringFromXML(xml `<foo/>`);
}

function getXML() returns xml = @java:Method {
    class:"org/ballerinalang/test/javainterop/RefTypeTests"
} external;

function getStringFromXML(xml x) returns string = @java:Method {
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

function testUseTypeDesc() returns string {
    return useTypeDesc(json);
}

function testGetTypeDesc() returns typedesc<any> {
    typedesc<any> td = getTypeDesc();
    return td;
}

function useTypeDesc(typedesc<any> t) returns string = @java:Method {
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

// Intersection types with interop.
function testDifferentRefTypesForIntersectionEffectiveType() {
    map<int> & readonly intMap = {a: 1, b: 2, e: 3};
    map<string> & readonly stringMap = {c: "hello", d: "world"};

    (int|string)[] & readonly readOnlyArr = getValues(intMap, stringMap); // Valid assignment.
    (int|string)[] arr = getValues(intMap, stringMap);

    assertEquality(5, arr.length());
    assertEquality(true, arr.isReadOnly());

    assertTrue(arr.indexOf(1) != ());
    assertTrue(arr.indexOf(2) != ());
    assertTrue(arr.indexOf(3) != ());
    assertTrue(arr.indexOf("hello") != ());
    assertTrue(arr.indexOf("world") != ());
    assertEquality((), arr.indexOf("ballerina"));
}

function getValues(map<int> & readonly intMap, map<string> & readonly stringMap)
    returns (int|string)[] & readonly = @java:Method {
        class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function testUsingIntersectionEffectiveType() {
    int a = 1;
    any & readonly r1 = echoAnydataAsAny(a);
    assertEquality(a, r1);

    Graduate b = new ("Jo", 1234);
    readonly & abstract object {} r2 = echoObject(b);
    assertEquality(b, r2);

    Details & readonly c = {
        emp: b,
        employed: true
    };
    boolean r3 = echoImmutableRecordField(c, "employed");
    assertTrue(r3);
}

type Graduate readonly object {
    string name;
    int id;

    function init(string name, int id) {
        self.name = name;
        self.id = id;
    }
};

type Details record {
    Graduate emp;
    boolean employed;
};

function echoAnydataAsAny(anydata & readonly value) returns any & readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function echoObject(abstract object {string name; int id;} & readonly obj)
    returns abstract object {} & readonly = @java:Method {
        class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function echoImmutableRecordField(Details & readonly value, string key) returns boolean = @java:Method {
     class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function testReadOnlyAsParamAndReturnTypes() {
    readonly r1 = acceptAndReturnReadOnly(1);
    assertTrue(r1 is int);
    assertEquality(100, r1);

    map<int|string|float> & readonly m0 = {first: 1.0, second: 2};
    readonly r2 = acceptAndReturnReadOnly(m0);
    assertTrue(r2 is float);
    assertEquality(1.0, r2);

    map<string> & readonly m1 = {
        third: "baz",
        first: "foo",
        second: "bar"
    };
    readonly r3 = acceptAndReturnReadOnly(m1);
    assertTrue(r3 is string);
    assertEquality("foo", r3);

    record { } & readonly m2 = {
        "third": "baz",
        "num": 21231
    };
    readonly r4 = acceptAndReturnReadOnly(m2);
    assertTrue(r4 is ());

    readonly object {
        int i = 21;

        function getInt() returns int {
            return self.i;
        }
    } ob = new;
    readonly r5 = acceptAndReturnReadOnly(ob);
    assertTrue(r5 is readonly & abstract object { int i; function getInt() returns int; });
    var cObj = <abstract object { int i; function getInt() returns int; } & readonly> r5;
    assertEquality(21, cObj.getInt());
    assertEquality(ob, r5);

    readonly & boolean[] arr = [true, false];
    readonly r6 = acceptAndReturnReadOnly(arr);
    assertTrue(r6 is readonly & boolean[2]);
    assertEquality(<boolean[]> [true, false], r6);
    assertEquality(arr, r6);
}

function acceptAndReturnReadOnly(readonly value) returns readonly = @java:Method {
     class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function testNarrowerTypesAsReadOnlyReturnTypes() {
    readonly r1 = getNilAsReadOnly();
    assertTrue(r1 is ());

    readonly r2 = getBooleanAsReadOnly();
    assertTrue(r2 is boolean);
    assertTrue(<boolean> r2);

    readonly r3 = getIntAsReadOnly();
    assertTrue(r3 is int);
    assertEquality(100, r3);

    float f = 12.3;
    readonly r4 = getFloatAsReadOnly(f);
    assertTrue(r4 is float);
    assertEquality(f, r4);

    decimal d = 120.5;
    readonly r5 = getDecimalAsReadOnly(d);
    assertTrue(r5 is decimal);
    assertEquality(d, <decimal> r5);

    string s1 = "hello";
    string s2 = "world";
    readonly r6 = getStringAsReadOnly(s1, s2);
    assertTrue(r6 is string);
    assertEquality(s1 + s2, r6);

    error e = error("Oops!");
    readonly r7 = getErrorAsReadOnly(e);
    assertTrue(r7 is error);
    assertEquality(e, r7);

    var func = function () returns int => 123;
    readonly r8 = getFunctionPointerAsReadOnly(func);
    assertTrue(r8 is function () returns int);
    assertEquality(func, r8);

    Bar bar = new (23);
    readonly r9 = getObjectOrServiceAsReadOnly(bar);
    assertTrue(r9 is Bar);
    assertEquality(bar, r9);

    typedesc t = typeof bar;
    readonly r10 = getTypedescAsReadOnly(t);
    assertTrue(r10 is typedesc);
    assertEquality(t, r10);

    handle h = getHandle();
    readonly r11 = getHandleAsReadOnly(h);
    assertTrue(r11 is handle);
    assertEquality(h, r11);

    'xml:Element & readonly x = xml `<Employee><name>Maryam</name></Employee>`;
    readonly r12 = getXmlAsReadOnly(x);
    assertTrue(r12 is readonly & xml);
    assertEquality(x, r12);

    int[] & readonly arr = [1, 2];
    readonly r13 = getListAsReadOnly(arr);
    assertTrue(r13 is int[] & readonly);
    assertEquality(arr, r13);

    [int, int...] & readonly tup = [1];
    readonly r14 = getListAsReadOnly(tup);
    assertTrue(r14 is [int] & readonly);
    assertEquality(tup, r14);

    map<int|string> & readonly mp = {a: 1, b: 2};
    readonly r15 = getMappingAsReadOnly(mp);
    assertTrue(r15 is map<int> & readonly);
    assertEquality(mp, r15);

    record {|string a;|} & readonly rec = {a: "hello world"};
    readonly r16 = getMappingAsReadOnly(rec);
    assertTrue(r16 is record {|string a;|} & readonly);
    assertEquality(rec, r16);

    table<map<int>> & readonly tb = table [
        {a: 1, b: 2},
        {a: 2, b: 20}
    ];
    readonly r17 = getTableAsReadOnly(tb);
    assertTrue(r17 is table<map<int>> & readonly);
    assertEquality(tb, r17);
}

type Bar readonly object {
    readonly int i;
    readonly string s = "hello world";

    function init(int i) {
        self.i = i;
    }
};

function getNilAsReadOnly() returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getBooleanAsReadOnly() returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getIntAsReadOnly() returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getFloatAsReadOnly(float f) returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getDecimalAsReadOnly(decimal d) returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getStringAsReadOnly(string s1, string s2) returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getErrorAsReadOnly(error e) returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getFunctionPointerAsReadOnly(function () returns int func) returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getObjectOrServiceAsReadOnly(object {} ob) returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getTypedescAsReadOnly(typedesc t) returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getHandleAsReadOnly(handle t) returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getXmlAsReadOnly(xml & readonly x) returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getListAsReadOnly(int[] list) returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getMappingAsReadOnly(map<int|string> mp) returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getTableAsReadOnly(table<map<int>> tb) returns readonly = @java:Method {
    class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
