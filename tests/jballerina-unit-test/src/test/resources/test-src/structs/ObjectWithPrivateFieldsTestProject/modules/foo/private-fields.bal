
public type userFoo record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "23468";
    string ssn = "";
};

public type user record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "23468";
};

public class userObject {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "23468";
}

public type person record {
    int age = 0;
    string name = "";
    string ssn = "";
    int id = 0;
};

type student record {
    int age = 0;
    string name = "";
    string ssn = "";
    int id = 0;
    int schoolId = 0;
};

public function newPerson() returns (person) {
    person p = {};
    p.age = 12;
    p.name = "mad";
    p.ssn = "234-90-8887";
    return p;
}

public function newUser() returns (user) {
    user u = {};
    u.age = 56;
    u.name = "mal";
    u.zipcode = "23126";
    return u;
}

public function newUserObject() returns userObject {
    userObject u = new;
    u.age = 56;
    u.name = "mal";
    u.zipcode = "23126";
    return u;
}