enum Color {
    RED, GREEN, BLUE
}

const float FVAL = 1.4;
type Foo FVAL|1|2|"test1";

type Employee record {
    readonly string name;
    int salary;
};

configurable table<Employee> key(name) t = table [
    { name: "John", salary: 100 },
    { name: "Jane", salary: 200 }
];
configurable map<int> item\ Codes = {"item1": 11, "item2": 12, "item3": 45};
configurable Color itemColor = RED;
configurable Foo val = 1;
configurable Color|Foo myVal = 1.4;
configurable map<int>[] myArray = [{"val1" : 22}, {"val2" : 32}];
configurable Employee[] employeeArray = ?;
configurable map<Employee> employeeMap = {"emp1": {name: "user1", salary: 1200}};
configurable map<Employee>[] empMapArray = ?;

public function main() {
}
