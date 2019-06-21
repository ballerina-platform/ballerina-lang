function testTooManyArgsToReturn2(string s) returns (string){
    return split(s);
}

function split(string s) returns [string, string] {
    return [s, ""];
}