type Person record {
    string name;
    int age;
    string address;
    string...
};

type Student record {
    string name;
    int age;
    string address;
    string class;
};

function testWithConstrainedRestParam() {
    json<Person> j = {name:"John", address:"Doe", age:20, height:5.5};
}

function testWithOpenRecord() {
    json<Student> j = {name:"John Doe", age:20, address:"London"};
}
