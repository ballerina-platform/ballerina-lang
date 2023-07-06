type Person record {|
    string name;
    int id;
|};

function test1() {
    Person p = {
        name: "John Doe",
        id: 1
    };
    
    var {name, id} = p;
    int size = name.length();
}
