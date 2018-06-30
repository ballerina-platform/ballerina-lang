
public type userPFoo object {
    public int age;
    public string name;
    public string address;
    public string zipcode = "23468";

    public new (age, name, address) {}

    public function getName () returns (string);

    public function getAge () returns (int);
};

public function userPFoo::getName () returns (string) {
    return self.name + ":userPFoo";
}

public function userPFoo::getAge () returns (int) {
    return self.age;
}
