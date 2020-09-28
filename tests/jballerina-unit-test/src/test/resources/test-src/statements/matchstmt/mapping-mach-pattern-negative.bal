string CONST1 = "str";

function testMappingMatchPatternNegative() returns string {

    boolean v1 = true;
    match v1 {
        { a : true } => { // pattern will not be matched
            return "Match";
        }
    }

    map<int> v2 = { a : 2 };
    match v2 {
        { a : "2" } => { // pattern will not be matched
            return "Match";
        }
        { x : 2, y : "2" } | { x : 3, y : "3"} => { // pattern will not be matched
            return "Match";
        }
        { c1 : CONST1, c2 : 2 } => { // pattern will not be matched
            return "Match";
        }
    }

    map<int|string> v3 = { a : 2, b : "2" };
    match v3 {
        { a : 2 , b : true } => { // pattern will not be matched
            return "Match";
        }
        { a : CONST1 , b : true } => { // pattern will not be matched
            return "Match";
        }
    }

    return "No match";

}