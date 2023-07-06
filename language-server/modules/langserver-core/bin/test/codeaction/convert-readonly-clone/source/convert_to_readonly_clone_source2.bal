
public function main() {
    record {} x = {};

    match x {
        var {a, b} if getValue(let int test = 23 in {}) =>  {
        }
        var {a, c} =>  {
        }
    }

    boolean d = true;
    
}

isolated function getValue(record {} rec) returns boolean {
    return false;
}
