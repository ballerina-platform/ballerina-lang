package req2;

public type userPB object {
    public {
        int age;
        string name;
        string address;
    }

    new () {}

    public function getName () returns (string);

    public function getAge () returns (int);
};

public function userPB::getName () returns (string) {
    return self.name;
}

public function userPB::getAge () returns (int) {
    return self.age;
}
