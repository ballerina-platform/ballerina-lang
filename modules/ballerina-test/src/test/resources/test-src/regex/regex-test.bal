import ballerina.regex;

function matches(string s, string r)(boolean) {
    regex:Regex reg = {};
    reg.regex = r;
    reg.compile();
    return reg.matches(s);
}

function findAll(string s, string r) (string[]) {
    regex:Regex reg = {};
    reg.regex = r;
    reg.compile();
    return reg.findAll(s);
}

function replaceAllRgx(string s, string r, string target) (string) {
    regex:Regex reg = {};
    reg.regex = r;
    reg.compile();
    return reg.replaceAll(s, target);
}

function replaceFirstRgx(string s, string r, string target) (string) {
    regex:Regex reg = {};
    reg.regex = r;
    reg.compile();
    return reg.replaceFirst(s, target);
}