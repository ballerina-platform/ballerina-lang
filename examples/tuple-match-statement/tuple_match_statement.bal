import ballerina/io;

public function main() {
    // In this example, there are four different variables are created and they will be used with
    // the `basicMatch()` function, which does the match.
    (string, int)|(float, string, boolean)|float a1 = 66.6;
    (string, int)|(float, string, boolean)|float a2 = ("Hello", 12);
    (float, boolean)|(float, string, boolean)|float a3 = (4.5, true);
    (string, int)|(float, string, boolean)|float a4 = (6.7, "Test", false);

    basicMatch(a1);
    basicMatch(a2);
    basicMatch(a3);
    basicMatch(a4);

    // In this example, there are five different variables created and they will be used
    // with the 'matchWithTypeGuard()' function, which does the match along with type guard conditions.
    (string, int)|(boolean, int)|(int, boolean)|int|float b1 = ("Hello", 45);
    (string, int)|(float, boolean)|(int, boolean)|int|float b2 = (4.5, true);
    (float, boolean)|(boolean, int)|(int, boolean)|int|float b3 = (false, 4);
    (string, int)|(float, boolean)|(int, boolean)|int|float b4 = (455, true);
    (float, boolean)|(boolean, int)|(int, boolean)|float b5 = 5.6;

    matchWithTypeGuard(b1);
    matchWithTypeGuard(b2);
    matchWithTypeGuard(b3);
    matchWithTypeGuard(b4);
    matchWithTypeGuard(b5);
}

// The following method uses structured tuple match patterns with different sizes. The given `match` expression
// will be checked for "isLike" relationship and will be matched at runtime.
function basicMatch(any a) {
    match a {
        // This pattern check is for the tuple type of three variables and types can be of any.
        var (s, i, b) => io:println("Matched with three vars : "
                                    + io:sprintf("%s", a));
        // This pattern check is for the tuple type of two variables and types can be of any.
        var (s, i) => io:println("Matched with two vars : "
                                    + io:sprintf("%s", a));
        // This pattern check is for single variable and type can be of any. This has to be the last pattern.
        var s => io:println("Matched with single var : "
                                    + io:sprintf("%s", a));
    }
}

// The following method uses structured tuple match patterns with different sizes along with the type guards. The given
// `match` expression will be checked for "isLike" relationship and also it will check the type guard for the pattern
// to match during the runtime.
function matchWithTypeGuard(any b) {
    match b {
        // This pattern check is for the tuple type of two variables and types has to be `string` and `int`.
        var (s, i) if (s is string && i is int) =>
           io:println("'s' is string and 'i' is int : " + io:sprintf("%s", b));
        // This pattern check is for the tuple type of two variables and the first variable should be of the type `float`.
        var (s, i) if s is float =>
           io:println("Only 's' is float : " + io:sprintf("%s", b));
        // This pattern check is for the tuple type of two variables and the second variable should be of the type `int`.
        var (s, i) if i is int =>
           io:println("Only 'i' is int : " + io:sprintf("%s", b));
        // This pattern check is for the tuple type of two variables without any type guard.
        var (s, i) => io:println("No type guard : " + io:sprintf("%s", b));
        // This pattern check is for a single variable and its type should be `float`.
        var s if s is float =>
           io:println("'s' is float only : " + io:sprintf("%s", b));
    }
}
