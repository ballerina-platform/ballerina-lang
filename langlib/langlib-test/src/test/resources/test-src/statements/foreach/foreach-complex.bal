string output = "";

function concatString (string value) {
    output = output + value;
}

function concatIntString (int i, string v) {
    output = output + i.toString() + ":" + v + " ";
}

function concatInt (int i) {
    output = output + i.toString() + " ";
}

function concatTwoInts (int i, int j) {
    output = output + i.toString() + ":" + j.toString() + " ";
}

type Week record {
    string[] days;
};

function testNestedForeach () returns (string) {
    Week w = {days:["mon", "tue", "wed", "thu", "fri"]};
    string[] people = ["tom", "bob", "sam"];
    output = "";
    int i = 0;
    foreach var s in w.days {
        concatIntString(i, s);
        foreach var k in people {
            concatIntString(i, k);
        }
        concatString("\n");
        i += 1;
    }
    return output;
}

function testIntRangeSimple(int a, int b) returns (string){
    int x = a;
    output = "";
    foreach var i in x ... b {
        concatInt(i);
    }
    return output;
}

function testIntRangeEmptySet() returns (string){
    output = "";
    int i = 0;
    foreach var j in 5 ... 0  {
        concatTwoInts(i, j);
        i += 1;
    }
    return output;
}

function testIntRangeSimpleArity2(int a, int b) returns (string){
    int x = a;
    output = "";
    int i = 0;
    foreach var j in x ... b {
        concatTwoInts(i, j);
        i += 1;
    }
    return output;
}

int gx = 0;

type data record {
    int sx;
};

function testIntRangeComplex() returns (string){
    data d = {sx : 10};
    output = "";
    foreach var i in gx ... d.sx {
        concatInt(i);
    }
    return output;
}
