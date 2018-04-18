type State "on"|"off"|1;

function testSingletonAssignmentNegative() returns State {
    State p = "on";
    int b = p;
    string c = p;
    return p;
}

type Numbers 3|2|1;

function testSingletonAssignmentNegativeCaseTwo() returns Numbers {
    Numbers p = 1;
    int b = p;
    string c = p;
    return p;
}

type STRING_LITERALS "one"|"two"|"thre";

function testSingletonAssignmentNegativeCaseThree() returns STRING_LITERALS {
    STRING_LITERALS l = "one";
    string k = l;
    int b = l;
    return l;
}