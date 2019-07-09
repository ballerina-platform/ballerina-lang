function matches(string s, string r) returns (boolean|error) {
    return s.matches(r);
}

function findAll(string s, string r) returns (string[]|error) {
    return s.findAll(r);
}

function replaceAllRgx(string s, string r, string target) returns (string|error) {
    return s.replaceAll(r, target);
}

function replaceFirstRgx(string s, string r, string target) returns (string|error) {
    return s.replaceFirst(r, target);
}

function invalidPattern(string r) returns (boolean|error) {
    string s = "test";
    return s.matches(r);
}

function multipleReplaceFirst(string s, string r, string target) returns (string|error) {
    var replacedString = s.replaceFirst(r, target);
    return replacedString.replaceFirst(r, target);
}


