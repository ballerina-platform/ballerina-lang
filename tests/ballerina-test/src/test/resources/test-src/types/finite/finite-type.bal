type State "on"|"off"

function finiteAssignmentStateType() returns State {
    State p = "on";
    if (p == "on") {
       p = "off";
    }
    return p;
}

type NumberSet 1|2|3|4|5

function finiteAssignmentNumberSetType() returns NumberSet {
    NumberSet n = 1;
    if (n == 1) {
       n = 5;
    }
    return n;
}

type StringOrInt int|string

function finiteAssignmentStringOrIntSetType() returns StringOrInt {
    StringOrInt si = getInt();
    if (si == getInt()) {
       si = "This is a string";
    }
    return si;
}

function getInt() returns int {
    return 1;
}

function getString() returns string {
    return "This is a string";
}

function finiteAssignmentStringOrIntSetTypeCaseTwo() returns StringOrInt {
    StringOrInt si = getString();
    if (si == getString()) {
       si = 111;
    }
    return si;
}

type Int int

function finiteAssignmentIntSetType() returns Int {
    Int si = getInt();
    if (si == getInt()) {
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
    State b = "on";
    if (a == b){
       return 1;
    }
    return 2;
}

function finiteAssignmentStateSameTypeComparisonCaseTwo() returns State {
    State a = "off";
    State b = "on";
    if (a != b){
       a = b;
       return a;
    }
    return b;
}