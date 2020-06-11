
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

    public function init (string name) {
        self.name = name;
    }

    public function getName() returns string {
        return self.name;
    }

    public function getAge() returns int {
        return self.age;
    }

    public function getZipcode() returns string {
        return self.zipcode;
    }
};

public type BarObj object {
    public int age = 0;
    public string name = "";

    public function getName() returns string {
        return self.name;
    }

    function getAge() returns int {
        return self.age;
    }
};
