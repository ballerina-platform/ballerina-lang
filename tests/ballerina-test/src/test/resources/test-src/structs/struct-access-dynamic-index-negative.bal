type Person {
    string name = "default first name";
    string lname;
    map adrs;
    int age = 999;
}

type StructField {
    string key;
}

function testExpressionAsStructIndex () returns (string) {
    StructField nameField = {key:"name"};
    Person emp = {name:"Jack", adrs:{"country":"USA", "state":"CA"}, age:25};
    return emp[nameField.key];
}
