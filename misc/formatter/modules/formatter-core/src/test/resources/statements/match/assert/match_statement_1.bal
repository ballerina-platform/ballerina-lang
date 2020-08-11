public function foo() {
   string[] animals = ["Cat", "Canine", "Mouse"];
   foreach string animal in animals {
       match animal {
           "Mouse" => {
           }
           "Dog"|"Canine" => {
           }
           _ => {
           }
       }
   }
}
