import ballerina/io;

type Person record {
    string name;
    int age;
    string country;
};

public function main() {

    string firstName;
    int personAge;
    map<anydata | error> otherDetails = {};{ name : io:firstName,   age :personAge   ,...otherDetails}=getPerson() ;

    { ...otherDetails } = getPerson();

    var  { name,age  } = getPerson();

    { name : io:firstName,
           age :personAge   ,...otherDetails}=getPerson() ;

    { name : io:firstName, age :personAge   ,
...otherDetails}=getPerson() ;

    { name : io:firstName,
                 age :personAge   ,     ...otherDetails}=getPerson() ;
}

function getPerson() returns Person {
    Person person = { name: "Peter", age: 28, country: "Sri Lanka", occupation: "Software Engineer" };
    return person;
}
