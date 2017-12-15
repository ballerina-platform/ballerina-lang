function matches(string s, string r)(boolean) {
    Regex reg = {pattern:r};
    return s.matchesWithRegex(reg);
}

function findAll(string s, string r) (string[]) {
    Regex reg = {pattern:r};
    return s.findAllWithRegex(reg);
}

function replaceAllRgx(string s, string r, string target) (string) {
    Regex reg = {pattern:r};
    return s.replaceAllWithRegex(reg, target);
}

function replaceFirstRgx(string s, string r, string target) (string) {
    Regex reg = {pattern:r};
    return s.replaceFirstWithRegex(reg, target);
}

function invalidPattern(string r)(boolean) {
    Regex reg = {pattern:r};
    string s = "test";
    return s.matchesWithRegex(reg);
}

function multipleReplaceFirst(string s, string r, string target) (string) {
    Regex reg = {pattern:r};
    s = s.replaceFirstWithRegex(reg, target);
    return s.replaceFirstWithRegex(reg, target);
}


