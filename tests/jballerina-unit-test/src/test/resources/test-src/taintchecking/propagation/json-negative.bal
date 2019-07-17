public function main (string... args) {
    // tainted map<json> - untainted field - field access.
    map<json> student = {name:"Ballerina", address:args[0]};
    secureFunction(checkpanic student.name, checkpanic student.name);

    // tainted map<json> - untainted field - index access.
    map<json> student1 = {name:"Ballerina", address:args[0]};
    secureFunction(student1["name"], student1["name"]);

    // tainted map<json> - tainted field - field access.
    map<json> student2 = {name:"Ballerina", address:args[0]};
    secureFunction(checkpanic student2.address, checkpanic student2.address);

    // tainted map<json> - tainted field - index access.
    map<json> student3 = {name:"Ballerina", address:args[0]};
    secureFunction(student3["address"], student3["address"]);

    // untainted map<json> - tainted assignment - field access.
    map<json> student4 = {name:"Ballerina", address:"Colombo"};
    student4["name"] = args[0];
    secureFunction(checkpanic student4.name, checkpanic student4.name);

    // untainted map<json> - tainted assignment - index access.
    map<json> student5 = {name:"Ballerina", address:"Colombo"};
    student5["name"] = args[0];
    secureFunction(student5["name"], student5["name"]);

    // untainted map<json> - tainted assignment - replaced with untainted data - field access
    map<json> student6 = {name:"Ballerina", address:"Colombo"};
    student6["name"] = args[0];
    student6["name"] = "BallerinaNew";
    secureFunction(checkpanic student6.name, checkpanic student6.name);

    // untainted map<json> - tainted assignment - replaced with untainted data - index access
    map<json> student7 = {name:"Ballerina", address:"Colombo"};
    student7["name"] = args[0];
    student7["name"] = "BallerinaNew";
    secureFunction(student7["name"], student7["name"]);

}

public function secureFunction (@untainted any secureIn, any insecureIn) {

}
