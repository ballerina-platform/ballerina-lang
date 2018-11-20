
public type officer object {
    public int age = 0;
    public string name = "hidden, private";
    public string address = "";
    public string zipcode = "23468";

    private int ssn;
};

public type userBar object {
    public int age = 0;
    public string name = "hidden, private";
    public string address = "";
    public string zipcode = "23468";

    private int ssn;

    public new (name) {}

    public function getName() returns (string);

    public function getAge() returns (int);

    public function getZipcode() returns (string);
};

function userBar.getName() returns (string) {
    return self.name;
}

function userBar.getAge() returns (int) {
    return self.age;
}

function userBar.getZipcode() returns (string) {
    return self.zipcode;
}

public type BarObj object {
    public int age = 0;
    public string name = "";

    public function getName() returns (string);

    function getAge() returns (int) {
        return self.age;
    }
};

function BarObj.getName() returns (string) {
    return self.name;
}
