type recType record {|
    int i;
    string s;
|};

type Person record {|
    string firstName;
    string lastName;
    int age;
|};

function testFunction() {
    Person p1 = {firstName: "Alex", lastName: "George", age: 33};
    Person p2 = {firstName: "John", lastName: "David", age: 35};
    Person p3 = {firstName: "Max", lastName: "Gomez", age: 33};
    
    Person[] personList = [p1, p2, p3];
    map<int> myMap = {a:10,b:20};
    recType r = {i: 0, s: "Hello World"};
    int rank = 2;
    boolean bool = true;
    
    var result = from var person in  personList
                 order by 
}
