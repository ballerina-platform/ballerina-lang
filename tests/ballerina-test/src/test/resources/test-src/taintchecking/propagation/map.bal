function main (string... args) {
    // untainted Json - untainted field - field access
    map student = {name:"Ballerina", address:"Colombo"};
    secureFunction(student.name, student.name);

    // untainted Json - untainted field - index access
    map student1 = {name:"Ballerina", address:"Colombo"};
    secureFunction(student1["name"], student1["name"]);
}

public function secureFunction (@sensitive any secureIn, any insecureIn) {

}
