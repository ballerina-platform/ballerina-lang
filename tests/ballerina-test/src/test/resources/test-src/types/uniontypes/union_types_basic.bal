import ballerina.io;

function testUnionTypeBasics1() returns (int | float | string, int | string){
    int | float | string  aaa = 12330;
    int | string  bbb = "string value";

    aaa = 12.0;
    bbb = "sameera";

    return aaa, bbb;
}

function testUnionTypeBasics2() returns ( int | float | string | boolean) {
    int | float | string | boolean ttt = getUnion("jayasoma");
    return ttt;
}


function getUnion (string | int | float si) returns (int | float | string) {
    return "union types";
}
