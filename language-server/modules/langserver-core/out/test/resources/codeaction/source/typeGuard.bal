import ballerina/io;

public function main(string... args) {
    foo();
    getBar();
    update_apple();
    get_orange();
}

function foo() returns (int | string | error) {
    error e = error("incompatible reason");
    return e;
}

function getBar() returns (error?) {
    error e = error("incompatible reason");
    return e;
}

function update_apple() returns (int|error) {
    error e = error("incompatible reason");
    return e;
}

function get_orange() returns (int | string | boolean) {
    error e = error("incompatible reason");
    return true;
}