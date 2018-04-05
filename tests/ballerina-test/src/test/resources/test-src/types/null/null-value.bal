function testXmlNull () returns (xml|(), xml|(), int) {
    xml|() x1;
    xml|() x2;
    int a;
    match (x2){
        () => a = 5;
        any => a = 0;
    }

    return (x1, x2, a);
}

function testJsonNull () returns (json, json, int) {
    json j1;
    json j2 = null;
    int a = 0;
    if (j2 == null) {
        a = 6;
    }
    return (j1, j2, a);
}

function testStructNull () returns (Person|(), Person|(), int) {
    Person|() p1;
    Person|() p2;
    int a;
    match (p1){
        () => a = 7;
        any => a = 0;
    }

    return (p1, p2, a);
}

type Person {
    string name;
};

function testArrayNull () returns (string[], Person[], int) {
    string[] s;
    Person[] p;
    int a = 0;
    if (p == null) {
        a = 9;
    }
    return (s, p, a);
}

function testArrayNotNull () returns (int) {
    Person[] p = [];
    int a = 0;
    if (p != null) {
        a = 9;
    }
    return a;
}

function testMapNull () returns (map, map, int) {
    map m1;
    map m2;
    int a = 0;
    if (m1 == null) {
        a = 10;
    }
    return (m1, m2, a);
}

function testNullArrayAccess () returns (string) {
    string[] fruits;
    return fruits[0];
}

function testNullMapAccess () returns (string) {
    map marks;
    string value;
    value = <string>marks["maths"];
    return value;
}

function testCastingNull (any j) returns (xml) {
    xml x;
    x = check <xml>j;

    return x;
}

function testFunctionCallWithNull () returns (any) {
    xml x;
    return foo(x);
}

function foo (xml x) returns (xml) {
    return x;
}

function testNullLiteralComparison () returns (boolean) {
    return (null == null);
}

function testNullLiteralNotEqualComparison () returns (boolean) {
    return (null != null);
}

function testReturnNullLiteral () returns (any) {
    return null;
}

function testNullInWorker () returns (any) {

    worker worker1 {
        json request;
        request -> worker2;

        json result;
        result <- worker2;

        return result;
    }

    worker worker2 {
        json resp;
        resp <- worker1;
        resp -> worker1;
    }
}

function testNullInForkJoin () returns (json, json) {
    json m = null;
    fork {
        worker foo {
            json resp1 = null;
            resp1 -> fork;
        }
    
        worker bar {
            json resp2 = {};
            resp2 -> fork;
        }
    } join (all) (map allReplies) {
        any[] temp;
        temp = check <any[]>allReplies["foo"];
        json m1;
        m1 = check <json>temp[0];
        temp = check <any[]>allReplies["bar"];
        json m2;
        m2 = check <json>temp[0];
        return (m1, m2);
    } timeout (30000) (map msgs) {
        return (null, null);
    }
}

//function testArrayOfNulls () returns (Person|null []) {
//    Person p1 = {};
//    Person p2 = {};
//    Person | null p3 = null;
//    Person p4 = {};
//    Person|null [] personArray = [p1, p2, p3, p4];
//    return personArray;
//}

function testMapOfNulls () returns (map) {
    string x1 = "<x1>test xml1</x1>";
    xml x2;
    xml | () x3;
    string x4 = "<x4>test xml4</x4>";
    map xmlMap = {"x1":x1, "x2":x2, "x3":x3, "x4":x4, "x5":null};
    return xmlMap;
}