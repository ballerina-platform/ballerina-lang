function (string, string) returns (string) lambda = (string x, string y) => (string) {
    string r = x + y;
    return r;
};

function name1(function (string, string) returns (string) param) {
    string h = param("", "");
}

function name2() {
    name1((string x, string y) => (string) {
        string r = x + y;
        return r;
    });
}
