function matches(string s, string r)(boolean) {
    Regex reg = {pattern:r};
    reg.compile();
    return s.matchesWithRegex(reg);
}

function findAll(string s, string r) (string[]) {
    Regex reg = {pattern:r};
    reg.compile();
    return s.findAllWithRegex(reg);
}

function replaceAllRgx(string s, string r, string target) (string) {
    Regex reg = {pattern:r};
    reg.compile();
    return s.replaceAllWithRegex(reg, target);
}

function replaceFirstRgx(string s, string r, string target) (string) {
    Regex reg = {pattern:r};
    reg.compile();
    return s.replaceFirstWithRegex(reg, target);
}