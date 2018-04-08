
@final string stringvalue = getString();

@Args{value : stringvalue}
function foo () {
    // do nothing
}

struct Argument {
    string value;
}

annotation Args Argument;

function getString() returns (string) {
    return "sample";
}
