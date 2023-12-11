class Student {
	private string name;
	private int age;

	public function init(string name, int age) {
	    self.name = name;
	    self.age = age;
    }
}

function testFunction() {
    Student james = new ("James", 25);
    Student clarke = new Student("James", 25);
}
