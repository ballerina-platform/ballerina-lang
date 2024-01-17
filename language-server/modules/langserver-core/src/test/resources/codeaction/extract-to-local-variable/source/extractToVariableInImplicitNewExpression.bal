class Person {
	string name;
	int age;
	
	public function init(string name, int age) {
	    self.name = name;
	    self.age = age;
    }
}

function testFunction() {
    Person person = new ("Anne", 25);
}
