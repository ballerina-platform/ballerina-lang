function matches(string s, string r) returns (boolean|error) {
    var result = s.matches(r);
    match result {
        boolean matched => return matched;
        error err => return err;
    }
}

function findAll(string s, string r) returns (string[]|error) {
    var result = s.findAll(r);
    match result {
        string[] regexMatches => return regexMatches;
        error err => return err;
    }
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


