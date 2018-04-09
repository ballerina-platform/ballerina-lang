import ballerina/io;

type Employee {
    int id;
    string name;
    float salary;
};

function main (string[] args) {
    //Create in memory table constrained by the Employee struct type.
    table < Employee> tb = table {};

    //Add some data rows to the table.
    Employee e1 = {id:1, name:"Jane", salary:300.50};
    Employee e2 = {id:2, name:"Anne", salary:100.50};
    Employee e3 = {id:3, name:"John", salary:400.50};
    Employee e4 = {id:4, name:"Peter", salary:150.0};

    tb.add(e1);
    tb.add(e2);
    tb.add(e3);
    tb.add(e4);

    //Print the table data.
    io:print("Table Data:");
    io:println(tb);

    //Access using foreach.
    foreach x in tb {
        io:println("Name: " + x.name);
    }

    //Find the average salary using iterable operations.
    float lowerAvgSal = tb.filter(isLowerSalary).map(getSalary).average();
    io:println("Average of Low salary:" + lowerAvgSal);

    //Delete rows matching to a given criteria.
    int count = tb.remove(isLowerSalary);
    io:println("Deleted row count:" + count);
    io:print("After Delete:");
    io:println(tb);

    //Convert to a json.
    var j = <json> tb;

    match j {
        json jsonRes => {
                       io:print("JSON:");
                       io:println(j);
                   }
        error err =>  io:println("error: " + err.message);
    }

    //Convert to a xml.
    var x = <xml> tb;

    match x {
        xml xmlRes => {
                   io:print("XML:");
                   io:println(x);
               }
        error err =>  io:println("error: " + err.message);
    }
}

function isLowerSalary (Employee p) returns (boolean) {
    return p.salary < 200;
}

function getSalary (Employee p) returns (float) {
    return p.salary;
}
