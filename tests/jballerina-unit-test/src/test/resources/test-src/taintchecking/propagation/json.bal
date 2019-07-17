public function main (string... args) {
    // untainted Json - untainted field - field access
    json student = {name:"Ballerina", address:"Colombo"};
    secureFunction(checkpanic student.name, checkpanic student.name);

    // untainted Json - untainted field - index access
    map<json> student1 = {name:"Ballerina", address:"Colombo"};
    secureFunction(student1["name"], student1["name"]);
}

public function secureFunction (@untainted any secureIn, any insecureIn) {

}
