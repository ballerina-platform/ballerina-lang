
struct Person {
  string name;
  map adrs;
  int age;
  Family family;
  }

struct Family {
   string spouse;
   int noOfChildren;
   string[] children;
   }

function nullAssignment1 () (xml) {
    xml x;
    x = null;
    return x;
}

function nullAssignment2 () (xml) {
    xml x = null;
    return x;
}

function nullAssignment3 () (xml) {
    xml x = getNull();
    return x;
}

function nullAssignment4 () (xml) {
    xml x;
    x = getNull();
    return x;
}

function nullCheck ()(boolean) {
    xml x;
    x = null;
    if (x == null) {
        return true;
    } else {
        return false;
    }
}

function notNullCheck ()(boolean) {
    xml x;
    x = null;
    if (x != null) {
        return true;
    } else {
        return false;
    }
}

function getNull()(xml) {
    return null;
}

function setStructNull () (Person) {
    Person p;
    p = null;
    return p;
}

function accessElementOfNullStruct () (string) {
    Person p;
    string name;
    p = null;
    name = p.name;
    return name;
}

function settingElementOfNullStruct () {
    Person p;
    p = null;
    p.name = "name";
}

function accessingStructElementOfNullStruct () {
    Person p;
    Family f;
    string spouse;
    p = {};
    p.name = "Jack";
    p.family = null;
    spouse = p.family.spouse;
}

function settingStructElementOfNullStruct () {
    Person p;
    Family f;
    p = {};
    p.name = "Jack";
    p.family = null;
    p.family.spouse = "test";
}

function accessElementInNullArray() {
    string[] nullArray = null;
    string name = nullArray[2];
}

function accessElementInNullMap() {
    map  nullMap = null;
    string name = nullMap["name"];
}

function setElementInNullArray() {
    string[] nullArray;
    nullArray[2] = "hello";
}

function setElementInNullMap() {
    map  nullMap;
    nullMap["name"] = "hello";
}

function callFunctionWithNULLValue1()(xml){
    xml x = null;
    xml y = testSignature(x);
    return y;
}

function callFunctionWithNULLValue2()(xml){
    xml x;
    xml y;
    x = null;
    y = testSignature(x);
    return y;
}

function testSignature(xml x)(xml) {
    if(x == null) {
    int a = 0;
    }
    return null;
}

function testSignature(xml x, json y)(xml) {
    if(x == null) {
        int a = 0;
    }
    return null;
}

function testSignature(json x)(xml) {
    if(x == null) {
        int a = 0;
    }
    return null;
}




