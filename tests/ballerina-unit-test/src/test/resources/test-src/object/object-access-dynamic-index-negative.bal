type Person object {
    public string name = "default first name";
    public string lname = "";
    public map<any> adrs = {};
    public int age = 999;

    function __init (string name, map<any> adrs, int age) {
        self.name = name;
        self.age = age;
        self.adrs = adrs;
    }
};

type ObjectField object {
    public string key = "";

    function __init (string key) {
        self.key = key;
    }
};

function testExpressionAsStructIndex () returns (string) {
    ObjectField nameField = new ("name");
    Person emp = new ("Jack", {"country":"USA", "state":"CA"}, 25);
    return emp[nameField.key];
}
