import ballerina/io;

public type Employee record {|
   string name;
   string...;
|};

public type Address record {|
   string street;
|};


public type Person record {
    string profession;
};

public type Human record {|
    float height;
|};

public type Student record {|
    *Person;
    string name;
    *Human;
    int age;

    record {|
        *Person;
        string name;
        *Human;
        int age;
        int score;
        float...;
    |} parent;

    int score;
    float...;
|};

public function main(string... args) {
    testRecords();
}

function testRecords(){
   Address addr = {street: "templers rd"}; // simple record
   Employee emp = {name: "john", ...addr}; // record with rest-field
   io:println(emp);

   Student student = {
        name: "saman",
        age: 20,
        parent: {
            name: "kamal",
            age: 30,
            score: -1,
            profession: "driver",
            height: 190
        },
        score: 90,
        profession: "student",
        height: 150
    };

    io:println(student);
}
