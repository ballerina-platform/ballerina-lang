struct Person {
    string name;
    int age;
}

function testConstantsInTransform () {
    string str1 = "Sherlock Holmes";
    boolean flag1 = true;
    xml x1 = xml`<book>The Lost World</book>`;
    json json1 = {name:"Alice"};
    string[] arr1 = [];
    map map1 = {};
    Person p1 = {};
    json<Person> jsonP1 = {name:"Alice", age:30};

    boolean b = <boolean, Foo(str1, flag1, x1, json1, arr1, map1, jsonP1)> p1;
}


function getPrefixedName(string name) returns (string) {
    return "Mr." + name;
}

transformer <Person p1, boolean e> Foo(string str1, boolean flag1, xml x1, json json1,
                                       string[] arr1, map map1, json<Person> jsonP1) {
    string str2 = "Sherlock Holmes";
    boolean flag2 = true;
    xml x2 = xml`<book>The Lost World</book>`;
    json json2 = {name:"Alice"};
    string[] arr2 = [];
    map map2 = {};
    Person p2 = {};
    json<Person> jsonP2 = {name:"Alice", age:30};
    str2 = getPrefixedName(str1);
    flag2 = flag1;
    x2 = x1;
    json2 = json1;
    arr2 = arr1;
    map2 = map1;
    p2 = p1;
    jsonP2 = jsonP1;
}