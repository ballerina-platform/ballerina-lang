type Person object {
    public {
        string name = "default first name";
        string lname;
        map adrs;
        int age = 999;
    }
    new (name, adrs, age) {

    }
};

type ObjectField object {
    public {
        string key;
    }
    new (key) {

    }
};

function testExpressionAsStructIndex () returns (string) {
    ObjectField nameField = new ("name");
    Person emp = new ("Jack", {"country":"USA", "state":"CA"}, 25);
    return emp[nameField.key];
}
