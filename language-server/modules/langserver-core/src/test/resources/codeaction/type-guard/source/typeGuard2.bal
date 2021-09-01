type Token SingleCharDelim|MultiCharDelim|Keyword;
type SingleCharDelim "+" | "-" ;
type MultiCharDelim "<<" | ">>" | CompoundAssignDelim;
type CompoundAssignDelim "+=" | "-=" ;
type Keyword KEY | "string";
const KEY = "int";

public function main(string... args) {
    Token? cur = getToken();
}
