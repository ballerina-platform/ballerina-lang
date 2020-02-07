function f1 (string data) returns @untainted string {
    return f2(data);
}

function f2 (string data) returns (string) {
    return f3(data);
}

function f3 (string data) returns (string) {
    return f1(data);
}
