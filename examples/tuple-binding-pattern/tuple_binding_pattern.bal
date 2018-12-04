import ballerina/io;

function main() {

    //Following is a simple binding pattern which involves only a single variable.
    (boolean, float) t = (true, 0.4);
    io:println("Simple variable : ", t);

    //The same variable definition above can be written using a tuple binding pattern with separate variables as below.
    (boolean, float) (a1, a2) = (true, 0.4);
    io:println("Tuple variable : ", a1, a2);

    //The binding patterns are recursive in nature. Following examples shows how we can write complex recursive
    //variable definitions.

    ((string, int), float) ((b1, b2), b3) = (("Ballerina", 4), 6.7);
    io:println("Tuple variable : ", b1, b2, b3);

    ((string, int), (boolean, float)) ((c1, c2),(c3, c4)) = (("Ballerina", 34), (true, 6.7));
    io:println("Tuple variable : ", c1, c2, c3, c4);

    //Tuple variables can also be defined using tuple type expressions as below.
    ((string, (int, (boolean, byte))), (float, int)) v1 = (("Ballerina", (3, (true, 34))), (5.6, 45));
    ((string, (int, (boolean, byte))), (float, int)) ((d1, (d2, (d3, d4))), (f, i2)) = v1;
    io:println("Tuple variable : ", d1, d2, d3, d4);

    //Tuple type can also take "var" as the type label, which in that case, the type will be inferred from the RHS.
    var (e1, (e2, e3)) = ("Ballerina", (123, true));
    io:println("Tuple variable : ", e1, e2, e3);

    var v2 = (("Ballerina", (3, (true, 34))), (5.6, 45));
    ((string, (int, (boolean, int))), (float, int)) ((f1, (f2, (f3, f4))), (f5, f6)) = v2;
    io:println("Tuple variable : ", f1, f2, f3, f4, f5, f6);

    //Tuple variable definitions can also take union types as below.
    (string|int|float, (string|float, string)) (g1, (g1, g3)) = (34, (6.7, "Test"));
    io:println("Tuple variable : ", g1, g2, g3);

    //Tuple variables can also updated with new values when they used as tuple variable reference based binding
    //pattern as below.
    (string, (int, boolean)) (i1, (i2, i3)) = ("Hello", (123, true));
    ((i1, i2), i3) = (("Ballerina", 453), false);
    io:println("Tuple variable : ", i1, i2, i3);

    //The variable can also be updated individually, because once they are defined, they are treated as individual
    //variables at runtime.
    (string, (int, boolean)) (j1, (j2, j3)) = ("Hello", (123, true));
    j1 = "Ballerina";
    j2 = 453;
    j3 = false;
    io:println("Tuple variable : ", j1, j2, j3);
}
