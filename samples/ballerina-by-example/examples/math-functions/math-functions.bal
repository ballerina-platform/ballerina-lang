import ballerina.lang.system;
import ballerina.lang.math;

function main (string[] args) {

    system:println("Value of PI : " + math:PI );
    system:println("Value of E  : " + math:E );
  
    //Returns the absolute value of a float value
    float absoluteFloatValue = math:absFloat(-152.2544);
    system:println("Absolute value of -152.2544 : " + absoluteFloatValue );

    //Returns the absolute value of an int value
    int absoluteIntValue = math:absInt(-152);
    system:println("Absolute value of -152      : " + absoluteIntValue );
    
    //Returns the arc cosine of a value
    float acosValue = math:acos(0.027415567780803774);
    system:println("Arc cosine of 0.027415567780803774  : " + acosValue);

    //Returns the arc sine of a value
    float arcSineValue = math:asin(0.027415567780803774);
    system:println("Arc sine of 0.027415567780803774    : " + arcSineValue);

    //Returns the arc tangent of a value
    float arcTangent = math:atan(0.027415567780803774);
    system:println("Arc tangent of 0.027415567780803774 : " + arcTangent);

    //Returns the cube root of a float value
    float cubeRoot = math:cbrt(-27);
    system:println("Cube root of 0.027415567780803774   : " + cubeRoot);

    //There are altogether 40+ methods in ballerina math API to perform numeric operations and you can find them
    //in "ballerina.lang.math" package.
}
