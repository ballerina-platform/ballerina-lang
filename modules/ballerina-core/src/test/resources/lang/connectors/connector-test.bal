connector Foo (string name, int age) {
    int userAge = 10 + age;
    string userName = "Ballerina" + name;

    action getUserName () (string) {
        return userName;
    }

    action getAge () (int) {
        return userAge;
    }
}

function getFooConnector(string name, int age) (Foo) {
    Foo foo = create Foo(name, age);
    return foo;
}

function testConnectorReturn (string name, int age) (string) {
    Foo conn = getFooConnector(name, age);
    string userName = conn.getUserName();
    int userAge = conn.getAge();
    return "Username: " + userName + ", Age: " + userAge;
}