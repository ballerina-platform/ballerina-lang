function (string s) returns string fn1 = function (string s) returns string {
    string _ = check getVal(s);
    return s;
};

public function main() returns error? {

    ["a", "b"].forEach(function(string s) {
        string _ = check getVal(s);
    });

    function _ = function (string s) {
        string _ = check getVal(s);
    };

    function _ = function (string s) returns string {
        string _ = check getVal(s);
        return s;
    };

    doSomething(function (string s) returns int|float {
        string _ = check getVal(s);
        return 0;
    });

}

function doSomething(function (string s) returns int|float param) {

}

function getVal(string s) returns string|error {
    return "";
}
