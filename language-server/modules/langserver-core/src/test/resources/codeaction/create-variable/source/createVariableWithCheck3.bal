public type Error1 E1|E2;
public type E1 distinct error;
public type E2 distinct error;

function foo1() {
    getValueOrError1();
}

function getValueOrError1() returns int|Error1 => 1;

public type E3 distinct error;
public type Error2 E3|int;
public type Error Error1|Error2;

function foo2() {
    getValueOrError2();
}

function getValueOrError2() returns int|Error => 1;

function foo3() {
    getValueOrError3();
}

function getValueOrError3() returns int|Error|Error1 => 1;

public type E E1 & E2;

function foo4() {
    getValueOrError4();
}

function getValueOrError4() returns int|E => 1;
