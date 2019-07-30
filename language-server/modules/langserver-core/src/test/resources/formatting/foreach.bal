string output = "";

public function name() {
    string[] fruits = ["apple", "banana", "cherry"];
           foreach var v in fruits {
   secureFunction(v, v);
 }
}

function testArrayWithTupleWithType() returns string {
    output = "";

    [int, string][] arr = [[1, "A"], [2, "B"], [3, "C"]];

  foreach [int, string] [i, v] in arr {
    int a = i;
       }
    return output;
}

function testArrayWithTupleInTupleWithoutType() returns string {
    output = "";

    [int, [string, float]][] arr = [[1, ["A", 2.0]], [2, ["B", 3.0]], [3, ["C", 4.0]]];foreach var [i, [s, f]] in arr {
    [string, float] isd = [s,f];
 }
    return output;
}

function testArrayWithTupleInTupleWithType() returns string {
    output = "";

    [int, [string, float]][] arr = [[1, ["A", 2.0]], [2, ["B", 3.0]], [3, ["C", 4.0]]];

 foreach [int, [string, float]] [i, [s, f]] in arr {
                  [int, [string, float]] asd = [i, [s,f]];}
    return output;
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

function testArrayWithTupleWithType() returns string {
    output = "";

    [int, string][] arr = [[1, "A"], [2, "B"], [3, "C"]];

     foreach   int   i  in   0...arr.count() - 1 {
          int a = i;
        }
    return output;
}