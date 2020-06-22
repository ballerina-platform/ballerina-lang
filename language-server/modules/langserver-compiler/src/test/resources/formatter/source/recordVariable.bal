type Person record {
    string name;
    int age;
    string country;
    string occupation;
};

public function main() {
    var  {  name  ,  age  }     =        getPerson(  )    ;
}

function getPerson() returns Person {
    Person person = {name: "Peter", age: 28, country: "Sri Lanka", occupation: "Software Engineer"};
    return person;
}