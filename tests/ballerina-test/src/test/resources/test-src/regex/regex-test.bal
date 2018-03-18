function matches(string s, string r)(boolean, error) {
    Regex reg = {pattern:r};
    boolean matched;
    error err;
    matched, err = s.matchesWithRegex(reg);
    return matched, err;
}

function findAll(string s, string r) (string[], error) {
    Regex reg = {pattern:r};
    string[] regexMatches;
    error err;
    regexMatches, err = s.findAllWithRegex(reg);
    return regexMatches, err;
}

function replaceAllRgx(string s, string r, string target) (string, error) {
    Regex reg = {pattern:r};
    string replacedString;
    error err;
    replacedString, err = s.replaceAllWithRegex(reg, target);
    return replacedString, err;
}

function replaceFirstRgx(string s, string r, string target) (string, error) {
    Regex reg = {pattern:r};
    string replacedString;
    error err;
    replacedString, err = s.replaceFirstWithRegex(reg, target);
    return replacedString, err;
}

function invalidPattern(string r)(boolean, error) {
    Regex reg = {pattern:r};
    string s = "test";
    boolean matched;
    error err;
    matched, err = s.matchesWithRegex(reg);
    return  matched, err;
}

function multipleReplaceFirst(string s, string r, string target) (string) {
    Regex reg = {pattern:r};
    s, _ = s.replaceFirstWithRegex(reg, target);
    s, _ = s.replaceFirstWithRegex(reg, target);
    return s;
}


