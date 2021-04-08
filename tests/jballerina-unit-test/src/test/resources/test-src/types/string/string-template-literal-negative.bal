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

function stringTemplate3() returns string {
    Foo foo = 4;
    return string`${foo}`;
}

function stringTemplate4() returns string {
    () foo = ();
    return string`${foo}`;
}

function stringTemplate5() returns string {
    int[]|string[] x = [1, 2, 3];
    return string`${x}`;
}
