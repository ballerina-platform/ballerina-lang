public function testDuplicate() {
}

public type Person record {|
    string name;
    int age;
|};

string testString = "test";

public class Company {
    string name;
    string address;

    public function init() {
    	 self.name = "test";
    	 self.address = "test";
    }
}

function testFunction() {
  testDuplicate();
    main();
}

function beforeFunc() {
}

function afterFunc() {
}
