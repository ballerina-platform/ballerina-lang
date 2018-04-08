import ballerina/io;

function testUnionTypeBasics1() returns (int | float | string, int | string){
    int | float | string  aaa = 12330;
    int | string  bbb = "string value";

    aaa = 12.0;
    bbb = "sameera";

    return (aaa, bbb);
}

function testUnionTypeBasics2() returns ( int | float | string | boolean) {
    int | float | string | boolean ttt = getUnion("jayasoma");
    return ttt;
}


function getUnion (string | int | float si) returns (int | float | string) {
    return "union types";
}


function testNullableTypeBasics1() returns (int | json | string | float | map | boolean | ()) {
    int | string | float | json| map | boolean | ()  k = 5;

    k = "sss";
    k = 1.0;

    json j = {name:"ddd"};
    k = j;

    map m = {name:"dddd"};
    k = m;

    k = true;

    k = ();
    return k;

}


function testNullableTypeBasics2() returns (int | boolean | ()) {

   int | float | () x;

  match x {
      float | int s => io:println("int");
      int | () s => io:println("null");
  }

  int | boolean | () i;

  match i {
          int => io:println("int");
          boolean => io:println("boolean");
          any | () a => io:println(a);
  }

  return i;

}