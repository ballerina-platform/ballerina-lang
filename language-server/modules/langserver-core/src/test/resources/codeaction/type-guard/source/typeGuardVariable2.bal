type Token SingleCharDelim|MultiCharDelim|Keyword;
type SingleCharDelim "+" | "-";
type MultiCharDelim "<<" | ">>" | CompoundAssignDelim  ;
type CompoundAssignDelim "+=" | "-=";
type Keyword KEY | "string" | 100 | "200" ;
const KEY = "int";

public function main(string... args) {
    Token? cur = getToken();
}

function getToken() returns Token? {
    return ();
}
