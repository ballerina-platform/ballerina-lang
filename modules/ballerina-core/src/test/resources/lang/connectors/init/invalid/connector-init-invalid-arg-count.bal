connector Foo(string name, int age) {

    action getName(string name) (string) {
        return name;
    }

    action getNameAndAge(string name, int age) (string, int) {
        return name, age;
    }
}

function testConnectorInit() (string, int){
    Foo f = create Foo("John");
    var s,i = f.getNameAndAge("sam", 50);
    return s,i;
}
