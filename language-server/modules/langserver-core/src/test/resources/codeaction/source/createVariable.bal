# An Apple Description
#
# + color - color of the apple
public type Apple object {
    public string color = "red";
};

# A Grapes Description
public type Grapes object {
     private string color;

     # creates new Grapes
     public function init(string color) {
        self.color = color;
     }
};

public function main (string... args) {
   int a = 12;
   int b = 13;
   addTwoIntegers(a, b);
   Apple apple = new Apple();
   apple.color;
   new Apple();
   new Grapes("red");
}

function addTwoIntegers(int a, int b) returns int {
   return 100;
}
