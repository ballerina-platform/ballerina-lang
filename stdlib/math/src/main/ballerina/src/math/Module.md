## Module overview

This module provides functions to perform fixed-precision integer arithmetic and fixed-precision decimal arithmetic. It includes functions to get the absolute, cosine, sine, root, tangent, and more for a given value.

## Sample
The sample given below uses a few functions that are in the `ballerina/math` module.

```ballerina
import ballerina/io;
import ballerina/math;

public function main(string... args) {

   // Get the value of Pi from the ‘ballerina/math module’.
   io:println("Value of Pi : " + math:PI.toString());

   // Get the value of E from the ‘ballerina/math module’.
   io:println("Value of E  : " + math:E.toString());

   // Get the absolute value of the given floating point number. 
   float absoluteFloatValue = math:absFloat(-152.2544);
   io:println("Absolute value of -152.2544 : " + absoluteFloatValue.toString());

   // Get the absolute value of an integer.
   int absoluteIntValue = math:absInt(-152);
   io:println("Absolute value of -152      : " + absoluteIntValue.toString());

   // Get the Arc cosine of a given value.
   float acosValue = math:acos(0.027415567780803774);
   io:println("Arc cosine of 0.027415567780803774  : " + acosValue.toString());

   // Get the Arc Sine value of a given value.
   float arcSineValue = math:asin(0.027415567780803774);
   io:println("Arc sine of 0.027415567780803774    : " + arcSineValue.toString());

   // Get the Arc Tangent value of a given value.
   float arcTangent = math:atan(0.027415567780803774);
   io:println("Arc tangent of 0.027415567780803774 : " + arcTangent.toString());

   // Calculate the cubic root of a given value.
   float cubeRoot = math:cbrt(-27);
   io:println("Cube root of -27   : " + cubeRoot.toString());
}
```
