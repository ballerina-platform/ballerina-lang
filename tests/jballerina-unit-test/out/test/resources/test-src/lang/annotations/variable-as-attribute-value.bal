
string stringvalue = getString();

@Args{value : stringvalue}
function foo () {
    // do nothing
}

annotation Args {
    string value;
}

function getString()(string) {
    return "sample";
}