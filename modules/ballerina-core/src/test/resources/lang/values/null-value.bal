
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
int k = 0;
    xml x = null;
    return x;
}

function nullAssignment2 () (xml) {
    xml x = null;
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


