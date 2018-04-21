
public type officer object {
    public {
        int age;
        string name = "hidden, private";
        string address;
        string zipcode = "23468";
    }
    private {
        int ssn;
    }
};

public type userBar object {
    public {
        int age;
        string name = "hidden, private";
        string address;
        string zipcode = "23468";
    }
    private {
        int ssn;
    }

    public new (name) {}

    public function getName() returns (string);

    public function getAge() returns (int);

    public function getZipcode() returns (string);
};

public function userBar::getName() returns (string) {
    return self.name;
}

public function userBar::getAge() returns (int) {
    return self.age;
}

public function userBar::getZipcode() returns (string) {
    return self.zipcode;
}

public type BarObj object {
    public {
        int age;
        string name;
    }

    public function getName() returns (string);

    function getAge() returns (int) {
        return age;
    }
};

public function BarObj::getName() returns (string) {
    return self.name;
}
