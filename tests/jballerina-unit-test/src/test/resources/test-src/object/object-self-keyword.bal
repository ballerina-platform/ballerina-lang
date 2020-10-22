
public function testObjectWithSelfKeyword () returns string {
    Person p = new Person();
    return p.getNameWrapper();
}

class Person {
    public int age = 10;
    public string name = "sample name";

    private int year = 50;
    private string month = "february";

    function getName() returns string {
        return self.name;
    }

    function getNameWrapper() returns string {
        return self.getName();
    }
}
