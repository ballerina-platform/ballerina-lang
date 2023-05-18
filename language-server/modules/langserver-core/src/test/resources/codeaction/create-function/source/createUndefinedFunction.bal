public function main (string... args) {
   int a = 12;
   int b = 13;
   float c = 10.1;
   addTwoIntegers(a, c, true);
   int d = addTwoIntegers2(a, b);
   a = assignInteger("string", b, true);
}
