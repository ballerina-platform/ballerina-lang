function stringTemplate1() returns (string) {
    string s = string `Hello ${name}`;
    return s;
}

function stringTemplate2() returns (string) {
    json name = {};
    string s = string `Hello ${name}`;
    return s;
}

public type Foo int|float|decimal|string|boolean|();

function tringTemplate3() returns string {
    Foo foo = 4;
    return string`${foo}`;
}

function tringTemplate4() returns string {
    () foo = ();
    return string`${foo}`;
}
