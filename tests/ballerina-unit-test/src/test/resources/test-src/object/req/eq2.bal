
public type userPFoo object {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "23468";

    public new (age, name, address) {}

    public function getName () returns (string);

    public function getAge () returns (int);
};

function userPFoo.getName () returns (string) {
    return self.name + ":userPFoo";
}

function userPFoo.getAge () returns (int) {
    return self.age;
}
