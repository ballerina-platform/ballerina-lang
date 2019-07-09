
int 'Variable\ Int = 800;
string 'Variable\ String = "value";
float 'Variable\ Float = 99.34323;
any 'Variable\ Any = 88343;
Person 'person\ 1 = {'first\ name: "Harry", 'last\ name:"potter", 'current\ age: 25};

public function 'getVariable\ Int() returns int {
    return 'Variable\ Int;
}

public function 'getVariable\ String() returns string {
    return 'Variable\ String;
}

public function 'getVariable\ Float() returns float {
    return 'Variable\ Float;
}

public function 'getVariable\ Any() returns any {
    return 'Variable\ Any;
}

public function 'getPerson\ 1() returns Person {
    return 'person\ 1;
}

public type Person record {
    string 'first\ name;
    string 'last\ name;
    int 'current\ age;
};
