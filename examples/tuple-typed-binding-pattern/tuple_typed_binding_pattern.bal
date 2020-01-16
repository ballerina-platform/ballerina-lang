import ballerina/io;

public function main() {

    // This is a simple binding pattern, which involves only a single variable.
    [boolean, float] t = [true, 0.4];
    io:println("Simple variable : ", t);

    // The same variable definition can be written using a tuple-binding pattern
    // with separate variables.
    [boolean, float] [a1, a2] = [true, 0.4];
    io:println("Tuple variable : ", a1, " ", a2);

    // The binding patterns are recursive in nature. These examples show
    // how to write complex recursive variable definitions.
    [[string, int], float] [[b1, b2], b3] = [["Ballerina", 4], 6.7];
    io:println("Tuple variable : ", b1, " ", b2, " ", b3);

    [[string, int], [boolean, float]] [[c1, c2],[c3, c4]] =
                                              [["Ballerina", 34], [true, 6.7]];
    io:println("Tuple variable : ", c1, " ", c2, " ", c3, " ", c4);

    // Tuple variables can also be defined using tuple-type expressions.
    [[string, [int, [boolean, byte]]], [float, int]] v1 =
                                   [["Ballerina", [3, [true, 34]]], [5.6, 45]];
    [[string, [int, [boolean, byte]]],
                           [float, int]] [[d1, [d2, [d3, d4]]], [d5, d6]] = v1;
    io:println("Tuple variable : ",
                    d1, " ", d2, " ", d3, " ", d4, " ", d5, " ", d6);

    // Tuple variable definitions can also take union types.
    [string|int|float, [string|float, int]] [g1, [g2, g3]] =
                                                    ["Ballerina", [3.4, 456]];
    io:println("Tuple variable : ", g1, " ", g2, " ", g3);
}
