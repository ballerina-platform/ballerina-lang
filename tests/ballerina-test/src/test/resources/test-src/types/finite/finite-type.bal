type State "on"|"off";

function finiteAssignmentStateType() returns State {
    State p = "on";
    if (p == "on") {
       p = "off";
    }
    return p;
}

type NumberSet 1|2|3|4|5;

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