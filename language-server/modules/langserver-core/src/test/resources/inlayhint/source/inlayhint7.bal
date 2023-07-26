type Person record {|
    string name;
    int age;
|};

public function main() {
    Person p = {
        name: "",
        age: 0
    };
    string testResult = test(10, ...p);
}

function test(int kind, string name, int age) returns string {
    return "Hello";
}
