function checkGreaterThanForUnsupportedType () returns (boolean) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 > j2;
}

function checkGreaterThanEualForUnsupportedType () returns (boolean) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 >= j2;
}


function checkLessThanForUnsupportedType () returns (boolean) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 < j2;
}

function checkLessThanEqualForUnsupportedType () returns (boolean) {
    json j1;
    json j2;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};

    return j1 <= j2;
}

function checkGreaterThan () returns (boolean) {
    int a = 0;
    string b = "";
    return a > b;
}

function checkGreaterThanEual () returns (boolean) {
    int a = 0;
    string b = "";
    return a >= b;
}

function checkLessThan () returns (boolean) {
    int a = 0;
    string b = "";
    return a < b;
}

function checkLessThanEqual () returns (boolean) {
    int a = 0;
    string b = "";
    return a <= b;
}

type Person record {|
    string name;
|};

Person person1 = {name: "Mike"};
Person person2 = {name: "John"};

function checkComparisonWithUnorderedTypes1() {
    Person p1 = {name: "Mike"};
    Person p2 = {name: "John"};
    boolean a1 = p1 < p2;
    boolean a2 = p1 <= p2;
    boolean a3 = p1 > p2;
    boolean a4 = p1 >= p2;
}

function checkComparisonWithUnorderedTypes2() {
    Person|int p1 = 12;
    Person|int p2 = 13;
    boolean a1 = p1 < p2;
    boolean a2 = p1 <= p2;
    boolean a3 = p1 > p2;
    boolean a4 = p1 >= p2;
}

function checkComparisonWithUnorderedTypes3() {
    Person[] p1 = [{name: "Mike"}];
    Person[] p2 = [{name: "John"}];
    boolean a1 = p1 < p2;
    boolean a2 = p1 <= p2;
    boolean a3 = p1 > p2;
    boolean a4 = p1 >= p2;
}

function checkComparisonWithUnorderedTypes4() {
    [Person, int] p1 = [{name: "Mike"}, 12];
    [Person, int] p2 = [{name: "John"}, 13];
    boolean a1 = p1 < p2;
    boolean a2 = p1 <= p2;
    boolean a3 = p1 > p2;
    boolean a4 = p1 >= p2;
}

function checkComparisonWithUnorderedTypes5() {
    [int, Person...] p1 = [12, {name: "Mike"}];
    [int, Person...] p2 = [13, {name: "John"}];
    boolean a1 = p1 < p2;
    boolean a2 = p1 <= p2;
    boolean a3 = p1 > p2;
    boolean a4 = p1 >= p2;
}

function checkComparisonWithDifferentStaticTypes1() {
    int a1 = 12;
    float b1 = 13.23;
    boolean x1 = a1 < b1;
    boolean x2 = a1 <= b1;
    boolean x3 = a1 > b1;
    boolean x4 = a1 >= b1;
}

function checkComparisonWithDifferentStaticTypes2() {
    int a1 = 12;
    decimal b1 = 5;
    boolean x1 = a1 < b1;
    boolean x2 = a1 <= b1;
    boolean x3 = a1 > b1;
    boolean x4 = a1 >= b1;
}

function checkComparisonWithDifferentStaticTypes3() {
    float a1 = 12.5;
    decimal b1 = 5;
    boolean x1 = a1 < b1;
    boolean x2 = a1 <= b1;
    boolean x3 = a1 > b1;
    boolean x4 = a1 >= b1;
}
