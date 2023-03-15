function testFunction(any v) returns string {
    match v {
        17 => {
            return "number";
        }
        _ => {
            return "any";
        }
    }
}
