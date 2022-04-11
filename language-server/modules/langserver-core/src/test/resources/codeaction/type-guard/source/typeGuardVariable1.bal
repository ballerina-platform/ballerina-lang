type Person record {
    string name?;
    int age = 0;
};

public function main() {
    Person person = {};
    string|int? val = createVar(person?.name)
}

function createVar(string val) returns string|int? {

}
