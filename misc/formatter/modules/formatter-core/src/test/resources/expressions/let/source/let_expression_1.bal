type Person record {
   int age;
};

function getPerson() returns Person => {
   age: 31
};

public function foo() {
   int a =    let    int b = 1 in b * 2;

   string greeting =let string hello = "Hello ",
                         string ballerina = "Ballerina!"in hello + ballerina;

   int three =    let int one = 1  , int two = one + one
   in
   one + two;

   int length =    let
   var num = 10,var txt = "four"     in
   num + txt.length();

   [int, int] v1 = [10, 20];
   int tupleBindingResult =let [int, int] [d1, d2] = v1,int d3 = d1 + d2
       in  d3 * 2;

   int age = let
   Person {
                   age: personAge
             } = getPerson()

             in
             personAge;
}
