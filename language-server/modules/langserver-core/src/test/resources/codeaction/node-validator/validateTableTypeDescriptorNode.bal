type Employee record {|
    readonly string name;
|};

public function main() {
    table <Employee> key(name) t = getTable();
}
