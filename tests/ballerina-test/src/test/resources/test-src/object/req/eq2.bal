package req;

public type userPFoo object {
    public {
        int age;
        string name;
        string address;
        string zipcode = "23468";
    }

    public new (age, name, address) {}

    public function getName () returns (string);

    public function getAge () returns (int);
}

public function userPFoo::getName () returns (string) {
    return name + ":userPFoo";
}

public function userPFoo::getAge () returns (int) {
    return age;
}