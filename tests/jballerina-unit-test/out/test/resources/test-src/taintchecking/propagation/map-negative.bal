public function main (string... args) {
    // tainted Json - untainted field - field access.
    map<any> student = {name:"Ballerina", address:args[0]};
    secureFunction(student.get("name"), student.get("name"));

    // tainted Json - untainted field - index access.
    map<any> student1 = {name:"Ballerina", address:args[0]};
    secureFunction(student1["name"], student1["name"]);

    // tainted Json - tainted field - field access.
    map<any> student2 = {name:"Ballerina", address:args[0]};
    secureFunction(student2.get("address"), student2.get("address"));

    // tainted Json - tainted field - index access.
    map<any> student3 = {name:"Ballerina", address:args[0]};
    secureFunction(student3["address"], student3["address"]);

    // untainted Json - tainted assignment - field access.
    map<any> student4 = {name:"Ballerina", address:"Colombo"};
    student4["name"] = args[0];
    secureFunction(student4.get("name"), student4.get("name"));

    // untainted Json - tainted assignment - index access.
    map<any> student5 = {name:"Ballerina", address:"Colombo"};
    student5["name"] = args[0];
    secureFunction(student5["name"], student5["name"]);

    // untainted Json - tainted assignment - replaced with untainted data - field access
    map<any> student6 = {name:"Ballerina", address:"Colombo"};
    student6["name"] = args[0];
    student6["name"] = "BallerinaNew";
    secureFunction(student6.get("name"), student6.get("name"));

    // untainted Json - tainted assignment - replaced with untainted data - index access
    map<any> student7 = {name:"Ballerina", address:"Colombo"};
    student7["name"] = args[0];
    student7["name"] = "BallerinaNew";
    secureFunction(student7["name"], student7["name"]);

}

public function secureFunction (@untainted any secureIn, any insecureIn) {

}
