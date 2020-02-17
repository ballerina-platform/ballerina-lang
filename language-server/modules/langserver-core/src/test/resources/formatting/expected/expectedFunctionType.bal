function (string, string) returns (string) lambda = function (string x, string y) returns (string) {
    string r = x + y;
    return r;
};

function name1(function (string, string) returns (string) param) {
    string h = param("", "");
}

function name2() {
    name1(function (string x, string y) returns (string) {
        string r = x + y;
        return r;
    });
}

function name3(
function (string, string) returns (string) param) {
    string h = param("", "");
}

function foo1(int x, function (string, int...) returns float bar)
returns float {
    return x * bar("2", 2, 3, 4, 5);
}

function foo2(int x, function (string,
int...) returns float bar)
returns float {
    return x * bar("2", 2, 3, 4, 5);
}

function foo3(int x,
function
(
string,
int...
)
returns
float
bar
)
returns float {
    return x * bar("2", 2, 3, 4, 5);
}

function foo4(int x, (function (string, int...) returns float) bar)
returns float {
    return x * bar("2", 2, 3, 4, 5);
}

function foo5(int x, (function (string,
int...) returns float) bar)
returns float {
    return x * bar("2", 2, 3, 4, 5);
}

function foo6(int x,
(
function
(
string,
int...
)
returns
float
)
bar
)
returns float {
    return x * bar("2", 2, 3, 4, 5);
}
