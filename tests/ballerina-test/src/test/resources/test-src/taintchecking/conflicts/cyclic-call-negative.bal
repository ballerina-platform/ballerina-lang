function f1 (string data) (string) {
    return f2(data);
}

function f2 (string data) (string) {
    return f3(data);
}

function f3 (string data) (string) {
    return f1(data);
}
