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