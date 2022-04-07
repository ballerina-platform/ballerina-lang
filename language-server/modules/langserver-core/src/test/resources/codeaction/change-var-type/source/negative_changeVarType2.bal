type Person record {
    string name?;
    int age = 0;
};

public function main() {
    Person person = {};
    string|int? val = createVar(name = person?.name)
}

function createVar(string name) returns string|int? {

}
