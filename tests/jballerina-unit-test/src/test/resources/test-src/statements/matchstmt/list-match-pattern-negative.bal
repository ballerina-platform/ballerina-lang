function testListMatchPatternNegative() returns string {
    any v1 = 0;
    match v1 {
        var a => {
            return "var a";
        }
        [1, 2] => { // unreachable pattern
            return "[1, 2]";
        }
    }

    any v2 = 0; // unreachable code
    match v2 {
        var a | [1, 2] => { // unreachable pattern, match patterns should contain same set of variables
            return "var a | [1, 2]";
        }
    }

    int v3 = 2; // unreachable code
    match v3 {
        [1, 2] => { // pattern will not be matched
            return "[1, 2]";
        }
        [var a, var b] => { // pattern will not be matched
            return "[var a, var b]";
        }
    }

    [int, int] v5 = [1, 2];
    match v5 {
        _ | [var a, var b] => { // unreachable pattern
            return "_ | [var a, var b]";
        }
    }

    return "No match"; // unreachable code
}

function testListOfMatchPatternsNegative() returns string {
    [int, int] v1 = [1, 2];
    match v1 {
        [var a, 2] | [var a, var b] => { // match patterns should contain same set of variables
            return "match1";
        }
    }

    [int, int] v2 = [1, 2];
    match v2 {
        [var a, 2] | [var a, var b] | [var a, var b, var c] => { // match patterns should contain same set of variables
                                                                 // pattern will not be matched
            return "match1";
        }
    }

    return "No match";
}