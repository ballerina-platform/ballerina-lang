
public type userPB object {
    public int age = 0;
    public string name = "";
    public string address = "";

    function __init () {}

    public function getName () returns (string);

    public function getAge () returns (int);
};

function userPB.getName () returns (string) {
    return self.name;
}

function userPB.getAge () returns (int) {
    return self.age;
}
