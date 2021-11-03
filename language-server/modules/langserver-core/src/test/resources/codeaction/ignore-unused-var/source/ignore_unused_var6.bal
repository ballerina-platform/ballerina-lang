type Person record {
    string name;
    int age;
};

public function main() {
    Person tom = {
        name: "Tom",
        age: 25
    };

    // Binding pattern    
    Person {name: _, age: _} = tom; 
}
