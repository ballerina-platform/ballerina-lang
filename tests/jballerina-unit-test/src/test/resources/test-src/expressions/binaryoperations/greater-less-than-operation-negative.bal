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

function checkComparisonWithUnorderedTypes6() {
    int|string a = 12;
    int|string b = 12;
    boolean x1 = a < b;
    boolean x2 = a <= b;
    boolean x3 = a > b;
    boolean x4 = a >= b;
}

type NumberSet 1|2|3|4|5.23;

function checkComparisonWithUnorderedTypes7() {
    NumberSet[] a = [1];
    NumberSet[] b = [2];
    boolean x1 = a < b;
    boolean x2 = a <= b;
    boolean x3 = a > b;
    boolean x4 = a >= b;
}

const float ONE = 1.0;
const int TWO = 2;

type OneOrTwo ONE|TWO;

function checkComparisonWithUnorderedTypes8() {
    OneOrTwo[] a = [1];
    OneOrTwo[] b = [2];
    boolean x1 = a < b;
    boolean x2 = a <= b;
    boolean x3 = a > b;
    boolean x4 = a >= b;
}

type FloatOrString float|string;

function checkComparisonWithUnorderedTypes9() {
    FloatOrString a = 12.12;
    FloatOrString b = 24.1;

    boolean x1 = a < b;
    boolean x2 = a <= b;
    boolean x3 = a > b;
    boolean x4 = a >= b;
}

type TwoInts 1|2;
type StringTenOrEleven "10"|"11";

function checkComparisonWithUnorderedTypes10() {
    TwoInts a = 1;
    StringTenOrEleven b = "10";

    boolean x1 = a < b;
    boolean x2 = a <= b;
    boolean x3 = a > b;
    boolean x4 = a >= b;
}

const float FIVE = 5.0;
const float SIX = 6.0;

type FiveOrSix FIVE|SIX;

function checkComparisonWithUnorderedTypes11() {
    FiveOrSix a = 5;
    TwoInts b = 1;

    boolean x1 = a < b;
    boolean x2 = a <= b;
    boolean x3 = a > b;
    boolean x4 = a >= b;
}

function checkComparisonWithUnorderedTypes12() {
    TwoInts|float a = 1;
    TwoInts? b = 2;

    boolean x1 = a < b;
    boolean x2 = a <= b;
    boolean x3 = a > b;
    boolean x4 = a >= b;
}

function checkComparisonWithUnorderedTypes13() {
    TwoInts a = 1;
    string b = "2";

    boolean x1 = a < b;
    boolean x2 = a <= b;
    boolean x3 = a > b;
    boolean x4 = a >= b;
}

function checkComparisonWithUnorderedTypes14() {
    string? a = "1";
    string|int b = "2";

    boolean x1 = a < b;
    boolean x2 = a <= b;
    boolean x3 = a > b;
    boolean x4 = a >= b;
}

function checkComparisonWithUnorderedTypes15() {
    [float, int, string] a = [10, 23, "ABC"];
    [float, int, float...] b = [10, 46];

    boolean x1 = a < b;
    boolean x2 = a <= b;
    boolean x3 = a > b;
    boolean x4 = a >= b;
}

function checkComparisonWithUnorderedTypes16() {
    [float, int, string, int...] a = [10, 23, "ABC"];
    [float, int, string, float...] b = [10, 46, "ABC"];

    boolean x1 = a < b;
    boolean x2 = a <= b;
    boolean x3 = a > b;
    boolean x4 = a >= b;
}

function checkComparisonWithUnorderedTypes17() {
    [float, int, string, float...] a = [10, 23, "ABC"];
    [float, int, float...] b = [10, 46];

    boolean x1 = a < b;
    boolean x2 = a <= b;
    boolean x3 = a > b;
    boolean x4 = a >= b;
}

function checkComparisonWithUnorderedTypes18() {
    byte|int:Signed32 a = -1;
    ()|null b = ();

    boolean _ = a < b;
    boolean _ = a <= b;
    boolean _ = a > b;
    boolean _ = a >= b;

    boolean _ = b < a;
    boolean _ = b <= a;
    boolean _ = b > a;
    boolean _ = b >= a;
}
