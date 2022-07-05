
public function main() {
    record {} x = {};
    int testValue = 1234;

    match x {
        var {a, b} if getValue(testValue > 2000 ? {x: 1234} : {x: 23}) =>  {
        }
        var {a, c} =>  {
        }
    }

    boolean d = true;
    
}

isolated function getValue(record {int x;} rec) returns boolean {
    return false;
}
