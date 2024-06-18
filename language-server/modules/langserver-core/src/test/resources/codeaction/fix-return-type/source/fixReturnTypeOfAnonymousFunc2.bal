function fn1 = function (string s) returns error {
    string _ = check getVal(s);
    return "string";
};

public function main() returns error? {
    function _ = function (string s) returns error {
        string _ = check getVal(s);
        return "string";
    };
}

function getVal(string s) returns string|error {
    return "";
}
