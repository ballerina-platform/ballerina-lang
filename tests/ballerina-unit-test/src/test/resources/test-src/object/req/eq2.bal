
public type userPFoo object {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "23468";

    public function __init (int age, string name, string address) {
        self.age = age;
        self.name = name;
        self.address = address;
    }

    public function getName () returns (string);

    public function getAge () returns (int);
};

public function userPFoo.getName () returns (string) {
    return self.name + ":userPFoo";
}

public function userPFoo.getAge () returns (int) {
    return self.age;
}
