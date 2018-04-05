type Student {
    string name;
    string address;
};

public function main (string[] args) {
    // tainted Json - untainted field - field access.
    Student student = {name:"Ballerina", address:args[0]};
    secureFunction(student.name, student.name);

    // tainted Json - untainted field - index access.
    Student student1 = {name:"Ballerina", address:args[0]};
    secureFunction(student1["name"], student1["name"]);

    // tainted Json - tainted field - field access.
    Student student2 = {name:"Ballerina", address:args[0]};
    secureFunction(student2.address, student2.address);

    // tainted Json - tainted field - index access.
    Student student3 = {name:"Ballerina", address:args[0]};
    secureFunction(student3["address"], student3["address"]);

    // untainted Json - tainted assignment - field access.
    Student student4 = {name:"Ballerina", address:"Colombo"};
    student4.name = args[0];
    secureFunction(student4.name, student4.name);

    // untainted Json - tainted assignment - index access.
    Student student5 = {name:"Ballerina", address:"Colombo"};
    student5.name = args[0];
    secureFunction(student5["name"], student5["name"]);

    // untainted Json - tainted assignment - replaced with untainted data - field access
    Student student6 = {name:"Ballerina", address:"Colombo"};
    student6.name = args[0];
    student6.name = "BallerinaNew";
    secureFunction(student6.name, student6.name);

    // untainted Json - tainted assignment - replaced with untainted data - index access
    Student student7 = {name:"Ballerina", address:"Colombo"};
    student7.name = args[0];
    student7.name = "BallerinaNew";
    secureFunction(student7["name"], student7["name"]);

}

public function secureFunction (@sensitive any secureIn, any insecureIn) {

}
