function fn1 = function (string s) {
    string _ = check getVal(s);
};

function fn2 = function (string s) returns string {
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

}

function getVal(string s) returns string|error {
    return "";
}
