
public type userFoo object {
    public {
        int age;
        string name;
        string address;
        string zipcode = "23468";
    }

    public function getName() returns (string);

    public function getAge() returns (int);
};

public function userFoo::getName () returns (string) {
    return self.name;
}

public function userFoo::getAge () returns (int) {
    return self.age;
}

public type user object {
    public {
        int age;
        string name;
        string address;
        string zipcode = "23468";
    }
};

public type person object {
    public {
        int age;
        string name;
    }
    private {
        string ssn;
        int id;
    }
};

type student object {
    public {
        int age;
        string name;
    }
    private {
        string ssn;
        int id;
        int schoolId;
    }
};

public function newPerson() returns (person) {
    person p = new;
    p.age = 12;
    p.name = "mad";
    p.ssn = "234-90-8887";
    return p;
}

public function newUser() returns (user) {
    user u = new;
    u.age = 56;
    u.name = "mal";
    u.zipcode = "23126";
    return u;
}

public type FooObj object {
    public {
        int age;
        string name;
    }

    public function getName() returns (string);
};

public function FooObj::getName() returns (string) {
    return self.name;
}
