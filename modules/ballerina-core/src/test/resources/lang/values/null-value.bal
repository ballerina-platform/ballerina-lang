import ballerina.lang.system;
public type Person {
  string name;
  map adrs;
  int age;
  Family family;
  }

public type Family {
   string spouse;
   int noOfChildren;
   string[] children;
   }

function nullAssignment () (xml) {
    xml x;
    x = null;
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
    p = new Person;
    p.name = "Jack";
    p.family = null;
    spouse = p.family.spouse;
}

function settingStructElementOfNullStruct () {
    Person p;
    Family f;
    p = new Person;
    p.name = "Jack";
    p.family = null;
    p.family.spouse = "test";
}


