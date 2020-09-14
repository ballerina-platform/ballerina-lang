function getHandle() returns handle? {
    handle? h = ();
    return h;
}

function getHandleValueAsAParameter(handle h) returns handle {
    return h;
}

function acceptHandleValueWithAny(any a) returns handle {
    handle h = <handle> a;
    return h;
}

function testHandleValueEquality(handle h1, handle h2) returns boolean {
    return h1 === h2;
}

function testHandleValueInequality(handle h1, handle h2) returns boolean {
    return h1 !== h2;
}

handle? modLevelH = ();

function setAndGetModuleLevelHandleValue(handle h) returns handle {
    modLevelH = h;
    return <handle> modLevelH;
}

function testUnionsWithHandleType(handle | string | int h) returns string {
    if (h is int) {
        return "int";
    } else if (h is string) {
        return "string";
    } else {
        return "handle";
    }
}

function testArrayAccessOfHandleValues(handle[] hArray, int index) returns handle {
    return hArray[index];
}

function testArrayStoreOfHandleValues(handle[] hArray, int index, handle h) returns handle[] {
    hArray[index] = h;
    return hArray;
}

function testCreateArrayOfHandleValues(handle h1, handle h2, handle h3, handle h4, int index) returns handle {
    handle[4] hArray = [h1, h2, h3, h4];
    return hArray[index];
}

function testCreateMapOfHandleValues(handle h1, handle h2, handle h3, handle h4) returns handle? {
    map<handle> hMap = {};
    hMap["key1"] = h1;
    hMap["key2"] = h2;
    hMap["key3"] = h3;
    hMap["key4"] = h4;
    return hMap["key2"];
}

type Material record {
    int id;
    string name;
    handle h;
};

function testCreateRecordWithHandleValues(handle h1, handle h2) returns handle {
    Material m = {id:10, name:"plastic", h:h1};
    m.h = h2;
    return m.h;
}

type MyList [int, handle, handle, string];

function testCreateTuplesWithHandleValues(handle h1, handle h2) returns handle {
    MyList list = [5, h1, h2, "this is a tuple value"];

    int i;
    string s;
    handle h3;
    handle h4;
    [i, h3, h4, s] = list;
    return h3;
}

class Person {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    handle data;

    function init (handle data) {
        self.data = data;
    }

    function getName () returns string {
        return self.name;
    }

    function getAge () returns int {
        return self.age;
    }

    function getData () returns handle {
        return self.data;
    }

    function setData(handle h) {
        self.data = h;
    }
}

function testCreateObjectWithHandleValues(handle h1, handle h2) returns handle {
    Person person = new(h1);
    person.setData(h2);
    return person.getData();
}
