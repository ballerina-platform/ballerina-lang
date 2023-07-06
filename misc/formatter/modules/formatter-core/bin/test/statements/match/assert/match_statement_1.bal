public function foo() {
    string[] animals = ["Cat", "Canine", "Mouse", "Horse"];
    foreach string animal in animals {
        match animal {
            "Mouse" => {
            }
            "Dog"|"Canine" => {
            }
            "Cat"|"Feline" => {
            }
            _ => {
            }
        }
    }
}
