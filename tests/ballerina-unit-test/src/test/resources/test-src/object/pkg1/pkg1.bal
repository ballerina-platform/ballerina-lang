
public type Employee object {
    public int age = 0;
    private string name = "";
    string email = "";

    public function getName() returns (string);

    private function getAge() returns (int);

    function getEmail() returns (string);
};


function Employee.getName() returns (string) {
    return self.name;
}

function Employee.getAge() returns (int) {
    return self.age;
}

function Employee.getEmail() returns (string) {
    return self.email;
}
