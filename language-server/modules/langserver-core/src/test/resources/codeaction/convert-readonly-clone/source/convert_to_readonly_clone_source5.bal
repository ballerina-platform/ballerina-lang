
public function main() {
    record {} x = {};
    int testValue = 1234;

    match x {
        var {a, b} if getValue(checkpanic getFirstValue()) =>  {
        }
        var {a, c} =>  {
        }
    }

    boolean d = true;
    
}

isolated function getValue(record {} rec) returns boolean {
    return false;
}

function getFirstValue() returns record {}|error {
    return {};
}
