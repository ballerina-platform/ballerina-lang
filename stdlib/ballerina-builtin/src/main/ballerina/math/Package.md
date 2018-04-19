## Package overview

This package provides functions to perform fixed-precision integer arithmetic and fixed-precision decimal arithmetic. It includes functions to get the absolute, cosine, sine, root, tangent, and more for a given value.

## Sample
The sample given below uses a few functions that are in the `ballerina/math` package.
Follow the steps given below to run the sample:

1. Copy the code given below to file and save it as `math.bal`.
```ballerina
import ballerina/io;
import ballerina/math;
function main(string[] args) {

   // Get the value of Pi from the ‘ballerina/math package’.
   io:println("Value of Pi : " + math:PI);
   // Get the value of E from the ‘ballerina/math package’.
   io:println("Value of E  : " + math:E);

   // Get the absolute value of the given floating point number. 
   float absoluteFloatValue = math:absFloat(-152.2544);
   io:println("Absolute value of -152.2544 : " + absoluteFloatValue);

   // Get the absolute value of an integer.
   int absoluteIntValue = math:absInt(-152);
   io:println("Absolute value of -152      : " + absoluteIntValue);

   // Get the Arc cosine of a given value.
   float acosValue = math:acos(0.027415567780803774);
   io:println("Arc cosine of 0.027415567780803774  : " + acosValue);
   
   // Get the Arc Sine value of a given value.
   float arcSineValue = math:asin(0.027415567780803774);
   io:println("Arc sine of 0.027415567780803774    : " + arcSineValue);

   // Get the Arc Tangent value of a given value.
   float arcTangent = math:atan(0.027415567780803774);
   io:println("Arc tangent of 0.027415567780803774 : " + arcTangent);

   // Calculate the cubic root of a given value.
   float cubeRoot = math:cbrt(-27);
   io:println("Cube root of -27   : " + cubeRoot);
}
```
2. Navigate to the directory where the `math.bal` file is saved via the terminal and run the file using the command given below.

  ```Ballerina run math.bal```

  The following output is given for each function that was used in the `math.bal` file.

```ballerina
  Value of Pi : 3.141592653589793
  Value of E  : 2.718281828459045
  Absolute value of -152.2544 : 152.2544
  Absolute value of -152      : 152
  Arc cosine of 0.027415567780803774  : 1.5433773235341761
  Arc sine of 0.027415567780803774    : 0.02741900326072046
  Arc tangent of 0.027415567780803774 : 0.0274087022410345
  Cube root of 0.027415567780803774   : -3.0
```
