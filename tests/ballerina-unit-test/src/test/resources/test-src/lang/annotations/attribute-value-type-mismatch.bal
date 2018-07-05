
@final int intValue = getInt();

@Args{value : intValue}
function foo () {
    // do nothing
}

annotation Args {
    string value;
}

function getInt()(int) {
    return 44;
}