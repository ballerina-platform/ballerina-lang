function testXmlNull () (xml, xml, int) {
    xml x1;
    xml x2 = null;
    int a = 0;
    if (x2 == null) {
        a = 5;
    }
    return x1, x2, a;
}

function testJsonNull () (json, json, int) {
    json j1;
    json j2 = null;
    int a = 0;
    if (j2 == null) {
        a = 6;
    }
    return j1, j2, a;
}

function testStructNull () (Person, Person, int) {
    Person p1;
    Person p2 = null;
    int a = 0;
    if (p2 == null) {
        a = 7;
    }
    return p1, p2, a;
}

struct Person {
    string name;
}

function testConnectorNull () (TestConnector, TestConnector, int) {
    TestConnector c1;
    TestConnector c2 = null;
    int a = 0;
    if (c2 == null) {
        a = 8;
    }
    return c1, c2, a;
}

// function testConnectorNotNull () (int) {
//     TestConnector c1 = create TestConnector();
//     int a = 1;
//     if (c1 != null) {
//         a = 8;
//     }
//     return a;
// }

connector TestConnector () {
    string name;

    action testAction () (string) {
        return name;
    }
}

function testArrayNull () (string[], Person[], int) {
    string[] s;
    Person[] p;
    int a = 0;
    if (p == null) {
        a = 9;
    }
    return s, p, a;
}

function testArrayNotNull () (int) {
    Person[] p = [];
    int a = 0;
    if (p != null) {
        a = 9;
    }
    return a;
}

function testMapNull () (map, map, int) {
    map m1;
    map m2 = null;
    int a = 0;
    if (m1 == null) {
        a = 10;
    }
    return m1, m2, a;
}

function testNullArrayAccess () (string) {
    string[] fruits;
    return fruits[0];
}

function testNullMapAccess () (string) {
    map marks;
    string value;
    value, _ = (string)marks["maths"];
    return value;
}

function testCastingNull (any j) (xml) {
    xml x;
    x, _ = (xml)j;

    return x;
}

function testFunctionCallWithNull () (any) {
    return foo(null);
}

function foo (xml x) (xml) {
    return x;
}

// function testActionInNullConenctor () {
//     endpoint<TestConnector> testConnector {}
//     string result = testConnector.testAction();
// }

function testNullLiteralComparison () (boolean) {
    return (null == null);
}

function testNullLiteralNotEqualComparison () (boolean) {
    return (null != null);
}

function testReturnNullLiteral () (any) {
    return null;
}

function testNullInWorker () (any) {

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

function testNullInForkJoin () (json, json) {
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
        temp, _ = (any[])allReplies["foo"];
        json m1;
        m1, _ = (json)temp[0];
        temp, _ = (any[])allReplies["bar"];
        json m2;
        m2, _ = (json)temp[0];
        return m1, m2;
    } timeout (30000) (map msgs) {
        return null, null;
    }
}

function testArrayOfNulls () (Person[]) {
    Person p1 = {};
    Person p2;
    Person p3 = null;
    Person p4 = {};
    Person[] personArray = [p1, p2, p3, p4, null];
    return personArray;
}

function testMapOfNulls () (map) {
    string x1 = "<x1>test xml1</x1>";
    xml x2;
    xml x3 = null;
    string x4 = "<x4>test xml4</x4>";
    map xmlMap = {"x1":x1, "x2":x2, "x3":x3, "x4":x4, "x5":null};
    return xmlMap;
}