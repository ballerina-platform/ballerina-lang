import ballerina/io;
import ballerina/math;

public function main() {

    io:println("Value of PI: ", math:PI);
    io:println("Value of E: ", math:E);

    // The [absFloat](https://ballerina.io/swan-lake/learn/api-docs/ballerina/math/functions.html#absFloat) function returns
    // the absolute value of a `float` value.
    float absoluteFloatValue = math:absFloat(-152.2544);
    io:println("Absolute value of -152.2544: ", absoluteFloatValue);

    // The [absInt](https://ballerina.io/swan-lake/learn/api-docs/ballerina/math/functions.html#absInt) function returns
    // the absolute value of an `int` value.
    int absoluteIntValue = math:absInt(-152);
    io:println("Absolute value of -152: ", absoluteIntValue);

    // The [acos](https://ballerina.io/swan-lake/learn/api-docs/ballerina/math/functions.html#acos) function
    // returns the arc cosine of a value.
    float acosValue = math:acos(0.027415567780803774);
    io:println("Arc cosine of 0.027415567780803774: ", acosValue);

    // The [asin](https://ballerina.io/swan-lake/learn/api-docs/ballerina/math/functions.html#asin) function
    // returns the arc sine of a value.
    float arcSineValue = math:asin(0.027415567780803774);
    io:println("Arc sine of 0.027415567780803774: ", arcSineValue);

    // The [atan](https://ballerina.io/swan-lake/learn/api-docs/ballerina/math/functions.html#atan) function
    // returns the arc tangent of a value.
    float arcTangent = math:atan(0.027415567780803774);
    io:println("Arc tangent of 0.027415567780803774: ", arcTangent);

    // The [cbrt](https://ballerina.io/swan-lake/learn/api-docs/ballerina/math/functions.html#cbrt) function
    // returns the cube root of a `float` value.
    float cubeRoot = math:cbrt(-27.0);
    io:println("Cube root of -27.0: ", cubeRoot);

    // There are over 40 [functions](https://ballerina.io/swan-lake/learn/api-docs/ballerina/math/index.html#functions) in the
    // ballerina math API that can be used to perform numeric operations.
}
