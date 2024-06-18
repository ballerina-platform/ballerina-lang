function fn1 = function (string s) {
    string _ = check getVal(s);
};

function (string s) returns string|error fn2 = function (string s) returns string {
    string _ = check getVal(s);
    return s;
};

public function main() returns error? {

    function (string s) returns string|error f2 = function (string s) returns string {
        string _ = check getVal(s);
        return s;
    };

}

function getVal(string s) returns string|error {
    return "";
}
