import ballerina/io;

type Person {
    int a;
    string fname = "John";
    string lname;
    Info|error info1;
    Info|() info2;
};

type Info {
    Address|error address1;
    Address|() address2;
};

type Address {
    string street;
    string city;
    string country = "Sri Lanka";
};

function testNonErrorPath () returns any {
    Address adrs = {city:"Colombo"};
    Info inf = {address1 : adrs};
    Person prsn = {info1 : inf};
    Person|error p = prsn;
    string|error|() x = p!info1!address1!city;
    return x;
}

function testNotNilPath () returns any {
    Address adrs = {city:"Colombo"};
    Info inf = {address2 : adrs};
    Person prsn = {info2 : inf};
    Person|() p = prsn;
    string|error|() x = p.info2.address2.city;
    return x;
}

function testErrorInMiddle () returns any {
    error e = {message:"custom error"};
    Info inf = {address1 : e};
    Person prsn = {info1 : inf};
    Person|error p = prsn;
    string|error|() x = p!info1!address1!city;
    return x;
}

function testErrorInFirstVar () returns any {
    error e = {message:"custom error"};
    Person|error p = e;
    string|error|() x = p!info1!address1!city;
    return x;
}

function testNilInMiddle () returns (any,any) {
    Info inf = {address2 : ()};
    Person prsn = {info2 : inf};
    Person|() p = prsn;
    string|error|() x = p.info1!address1!city;
    string|error|() y = p.info2.address2.city;
    return (x, y);
}

function testNilInFirstVar () returns (any,any) {
    Person|() p;
    string|error|() x = p.info1!address1!city;
    string|error|() y = p.info2.address2.city;
    return (x, y);
}

function testSafeNavigatingNilJSON_1 () returns any {
    json j;
    return j.foo;
}

function testSafeNavigatingNilJSON_2 () returns any {
    json j;
    return j["foo"];
}

function testSafeNavigatingNilJSON_3 () returns any {
    json j;
    return j.foo.bar;
}

function testSafeNavigatingNilJSON_4 () returns any {
    json j;
    return j["foo"]["bar"];
}

function testSafeNavigatingJSONWithNilInMiddle_1 () returns any {
    json j = {name:"hello"};
    return j.info["name"].fname;
}

function testSafeNavigatingJSONWithNilInMiddle_2 () returns any {
    json j = {name:"hello"};
    return j["info"].name["fname"];
}

function testSafeNavigatingNilMap () returns any {
    map m;
    return m["foo"];
}

function testSafeNavigatingWithFuncInovc_1 () returns any {
    string|error|() x = getNullablePerson().info1!address1!city;
    return x;
}

function testSafeNavigatingWithFuncInovc_2 () returns string|() {
    json j;
    string|() x = j.getKeys()[0];
    return x;
}

function getNullablePerson() returns Person|() {
    Info inf = {address2 : ()};
    Person prsn = {info2 : inf};
    Person|() p = prsn;
    return p;
}

type Employee object {
  public function getName() returns string {
     return ("John");
  }
};

function testSafeNavigationOnObject_1() returns string {
  Employee? p;

  p = new Employee();
  return p.getName() but { () => "null name"};
}

function testSafeNavigationOnObject_2() returns string {
  Employee? p;
  return p.getName() but { () => "null name"};
}

function testSafeNavigationOnObject_3() returns string {
  Employee|error p;
  p = new Employee();
  return p!getName() but { error => "error name"};
}

function testSafeNavigationOnObject_4() returns string {
  error e = {};
  Employee|error p = e;
  return p!getName() but { error => "error name"};
}

function testSafeNavigateArray_1() returns Person? {
    Person[]|() p;
    return p[0];
}

function testSafeNavigateArray_2() returns string? {
    Person[]|() p;
    return p[0].info2.address2.city;
}

function testNullLiftingOnError() returns string {
    error e;
    return e.message;
}

function testSafeNavigateOnErrorOrNull() returns string? {
    error|() e;
    return e.message;
}

function testSafeNavigateOnJSONArrayOfArray() returns json {
    json j = {"values" : [ ["Alex", "Bob"] ] };
    return j.values[0][1];
}

function testNilLiftingOnLHS_1() returns json {
    json j = {};
    j.info["address"].city = "Colombo";
    return j;
}

function testNilLiftingOnLHS_2() returns json {
    json j = [null];
    j[0].address.city = "Colombo";
    return j;
}

function testNonExistingMapKeyWithIndexAccess() returns string? {
    map<string> m;
    return m["a"];
}

function testNonExistingMapKeyWithFieldAccess() returns string {
    map<string> m;
    return m.a;
}
