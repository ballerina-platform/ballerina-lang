struct Person {
    string name;
    int age;
    string address;
}

struct Student {
    string name;
    int age;
    string address;
    string class;
}


function getStudent() (json){
    json<Student> j = {name:"John Doe", age:30, address:"Colombo", class:"5"};
    return (string)j["bus"];
}
