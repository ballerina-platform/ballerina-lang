function name1() returns int {return    0;}

function name2() returns int {int a = 0;return a;}

function name3() returns  byte  |  error  {
    var byteValue = <byte>2;
    return check byteValue;
}

function name4() returns [int, string] {
       return [0,
          "Marcus"];
}

function name5() returns [int, string] {
    int id = 0;
    string name = "marcus";
       return [id,
           name];
}