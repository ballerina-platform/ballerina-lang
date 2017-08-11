connector Foo(string name, int age) {

    action getName(Foo f, string name) (string) {
        return name;
    }

    action getNameAndAge(Foo f, string name, int age) (string, int) {
        return name, age;
    }
}

function testConnectorInit() (string, int){
    Foo f = create Foo("John");
    var s,i = Foo.getNameAndAge(f, "sam", 50);
    return s,i;
}
