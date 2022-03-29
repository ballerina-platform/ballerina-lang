function main (string... args) {
   int a = 12;
   int b = 13;
   addTwoIntegers(a, b);
   addTwoIntegers2(a, b);
   addTwoIntegers3(a, b);
}

function addTwoIntegers(int a, int b) returns int {
   return 100;
}

function addTwoIntegers2(int a, int b) returns [int,string] {
   return [100, ""];
}

function addTwoIntegers3(int a, int b) returns (int[]) {
   int[] a = [];
   return a;
}
