int globalVar = 12;

function testLet() {
   int b = let int x = 4 in 2 * x * globalVar;
   int y = 12;
   int b1 = let int x = globalVar * 2, int z = 5 in z * x * globalVar;
   int b2 = let int x = 4, int z = func(y + y * 2 + globalVar) in z * (x + globalVar + y);
}

function func(int a) returns int {
   return 1 + a;
}
