import variable;

function test() {

    string s = variable:address;

    variable:name = "abc";

    int i = variable:name;

    CD cd = variable:A;

    string a = variable:A;

    AB ab = variable:A;
}

type AB "A"|"B";

type CD "C"|"D";
