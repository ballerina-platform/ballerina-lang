function matches(string s, string r) returns (boolean | error) {
    Regex reg = {pattern:r};
    var result = s.matchesWithRegex(reg);
    match result {
        boolean matched => return matched;
        error err => return err;
    }
}

function findAll(string s, string r) returns (string[]| error) {
    Regex reg = {pattern:r};
    var result = s.findAllWithRegex(reg);
    match result {
        string[] regexMatches => return regexMatches;
        error err => return err;
    }
}

function replaceAllRgx(string s, string r, string target) returns (string | error) {
    Regex reg = {pattern:r};
    var result = s.replaceAllWithRegex(reg, target);
    match result {
        string replacedString => return replacedString;
        error err => return err;
    }
}

function replaceFirstRgx(string s, string r, string target) returns (string | error) {
    Regex reg = {pattern:r};
    var result = s.replaceFirstWithRegex(reg, target);
    match result {
        string replacedString => return replacedString;
        error err => return err;
    }
}

function invalidPattern(string r) returns (boolean | error) {
    Regex reg = {pattern:r};
    string s = "test";
    var result = s.matchesWithRegex(reg);
    match result {
        boolean matched => return matched;
        error err => return err;
    }
}

function multipleReplaceFirst(string s, string r, string target) returns (string | error) {
    Regex reg = {pattern:r};
    var result1 = s.replaceFirstWithRegex(reg, target);
    match result1 {
        string replacedString => {
            var result2 = replacedString.replaceFirstWithRegex(reg, target);
            match result2 {
                string finalString => return finalString;
                error err => return err;
            }
        }
        error err => return err;
    }
}


