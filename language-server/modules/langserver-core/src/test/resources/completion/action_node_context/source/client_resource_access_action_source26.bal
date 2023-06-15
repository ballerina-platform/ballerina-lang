type Pet record {|
    string name;
    int age;
|};

client class MyClass {
    resource function post user(int id, string name, int age) {

    }

    resource function get users(int... id) {

    }

    resource function post sports(int id, string... sport) {

    }

    resource function post pet(*Pet pet) {

    }
}

function name() {
    MyClass cl = new;
    string myString;
    int myInt;
    cl -> /user.post();
    cl -> /user.post(10,);
    cl -> /user.post(id=);
    cl -> /user.post(id=10,);
    cl-> /users(10,);
    cl-> /sports.post(10,"cricket",);
    cl-> /pet.post();
    cl-> /pet.post(name=10,);
}

public function getInt() returns int {
    return 1;
}

public function getString() returns string {
    return "";
}
