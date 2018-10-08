const string name = "Ballerina";

const age = 10;

public const id = 123;

function getName() returns string {
    return name;
}

function getAge() returns int {
    return age;
}

function getId() returns int {
    return id;
}

function concatConstants() returns string {
    return "Hello " + name;
}
