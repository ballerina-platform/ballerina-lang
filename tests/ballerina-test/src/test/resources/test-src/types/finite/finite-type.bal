type State "on"|"off";

function finiteAssignmentStateType() returns State {
    State p = "on";
    if (p == "on") {
       p = "off";
    }
    return p;
}

type NumberSet 1|2|3|4|5;

type SN State|NumberSet;

type SNR SN;

function finiteAssignmentCompositeFiniteTypes() returns SNR {
    SNR v1 = "on";
    SNR v2 = 5;
    SNR v3;
    if (v1 != v2) {
       v3 = "off";
    }
    return v3;
}

function finiteTypeSetIntersectionCaseOne() returns State {
    SNR v1 = "on";
    State v2 = "off";
    State v3;
    if (v1 != v2) {
       v3 = "off";
    }
    return v3;
}

function finiteTypeSetIntersectionCaseTwo() returns int {
    int v1 = 2;
    NumberSet v2 = 1;
    int v3;
    if (v1 != v2) {
       v3 = 100;
    }
    return v3;
}

function finiteTypeSetIntersectionCaseThree() returns string {
    string v1 = "off";
    State v2 = "off";
    string v3 = "good";
    if (v1 == v2) {
       v3 = "very good";
    }
    return v3;
}

function finiteAssignmentNumberSetType() returns NumberSet {
    NumberSet n = 1;
    if (n == 1) {
       n = 5;
    }
    return n;
}

type StringOrInt int|string;

function finiteAssignmentStringOrIntSetType() returns StringOrInt {
    StringOrInt si = 1;
    if (si == 1) {
       si = "This is a string";
    }
    return si;
}

function finiteAssignmentStringOrIntSetTypeCaseTwo() returns StringOrInt {
    StringOrInt si = "This is a string";
    if (si == "This is a string") {
       si = 111;
    }
    return si;
}

type Int int;

function finiteAssignmentIntSetType() returns Int {
    Int si = 1;
    if (si == 1) {
       si = 222;
    }
    return si;
}

function finiteAssignmentIntArrayType() returns Int {
    Int[] si = [];
    si[0] = 10001;
    si[1] = 2345;
    if (si[1] == 2345){
        si[1] = 9989;
    }
    return si[1];
}

function finiteAssignmentStateSameTypeComparison() returns int {
    State a = "off";
    if (a == "on"){
       return 1;
    }
    return 2;
}

function finiteAssignmentStateSameTypeComparisonCaseTwo() returns State {
    State a = "off";
    if (a != "on"){
       a = "on";
       return a;
    }
    return "on";
}

type POrInt Person|int;

type Person {
   string name;
};

function finiteAssignmentRefValueType() returns POrInt {
    Person p = {name:"abc"};
    POrInt pi = p;
    return pi;
}

function finiteAssignmentRefValueTypeCaseTwo() returns POrInt {
    Person p = {name:"abc"};
    POrInt pi = 4;
    return pi;
}