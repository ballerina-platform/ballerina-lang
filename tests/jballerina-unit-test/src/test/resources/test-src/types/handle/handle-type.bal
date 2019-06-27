import ballerina/io;

function getHandle() returns ophandle? {
    ophandle? h = ();
    return h;
}

function getHandleValueAsAParameter(ophandle h) returns ophandle {
    return h;
}

function acceptHandleValueWithAny(any a) returns ophandle {
    ophandle h = <ophandle> a;
    return h;
}

function testHandleValueEquality(ophandle h1, ophandle h2) returns boolean {
    return h1 === h2;
}

function testHandleValueInequality(ophandle h1, ophandle h2) returns boolean {
    return h1 !== h2;
}

ophandle? modLevelH = ();

function setAndGetModuleLevelHandleValue(ophandle h) returns ophandle {
    modLevelH = h;
    return <ophandle> modLevelH;
}

function testUnionsWithHandleType(ophandle | string | int h) returns string {
    if (h is int) {
        return "int";
    } else if (h is string) {
        return "string";
    } else {
        return "handle";
    }
}

function testArrayAccessOfHandleValues(ophandle[] hArray, int index) returns ophandle {
    return hArray[index];
}

function testArrayStoreOfHandleValues(ophandle[] hArray, int index, ophandle h) returns ophandle[] {
    hArray[index] = h;
    return hArray;
}

function testCreateArrayOfHandleValues(ophandle h1, ophandle h2, ophandle h3, ophandle h4, int index) returns ophandle {
    ophandle[4] hArray = [h1, h2, h3, h4];
    return hArray[index];
}

function testCreateMapOfHandleValues(ophandle h1, ophandle h2, ophandle h3, ophandle h4) returns ophandle {
    map<ophandle> hMap = {};
    hMap["key1"] = h1;
    hMap["key2"] = h2;
    hMap["key3"] = h3;
    hMap["key4"] = h4;
    return hMap.key2;
}

type Material record {
    int id;
    string name;
    ophandle h;
};

function testCreateRecordWithHandleValues(ophandle h1, ophandle h2) returns ophandle {
    Material m = {id:10, name:"plastic", h:h1};
    m.h = h2;
    return m.h;
}

type MyList [int, ophandle, ophandle, string];

function testCreateTuplesWithHandleValues(ophandle h1, ophandle h2) returns ophandle {
    MyList list = [5, h1, h2, "this is a tuple value"];

    int i;
    string s;
    ophandle h3;
    ophandle h4;
    [i, h3, h4, s] = list;
    return h3;
}

type Person object {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    ophandle data;

    function __init (ophandle data) {
        self.data = data;
    }

    function getName () returns string {
        return self.name;
    }

    function getAge () returns int {
        return self.age;
    }

    function getData () returns ophandle {
        return self.data;
    }

    function setData(ophandle h) {
        self.data = h;
    }
};

function testCreateObjectWithHandleValues(ophandle h1, ophandle h2) returns ophandle {
    Person person = new(h1);
    person.setData(h2);
    return person.getData();
}