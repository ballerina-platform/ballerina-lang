const string name = "Ballerina";

const age = 10;

function getName() returns string {
    return name;
}

function getAge() returns int {
    return age;
}

function concatConstants() returns string {
    return "Hello " + name;
}
