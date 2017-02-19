# Testerina

Testerina is the test framework built for the Ballerina language.  
This will be a part of ```ballerina-tools-0.8.0.zip``` distribution [1].

Testerina implements ```ballerina test``` command.  

To test a file written in Ballerina language use the test command as follows.  
```./ballerina test <package_name>```

Your test file should contain ```_test.bal``` suffix.

Once you run the tests using ballerina ```test``` command,  
Testerina will print a summary of test results on console.

#### Testerina supports following assert functionality.  

 - assertTrue (boolean)
 - assertFalse (boolean)
 - assertEquals (string, int, float, boolean, string[], int[], float[])

### Writing ballerina tests

- Test functions should contain the prefix ```test```.
- Each test function may contain one or more asserts.
- Once package may contain more than one *._test.bal files.

#### Tutorial

1 Download ```ballerina-tools-0.8.0.zip``` distribution and unzip.  
2 Unzip and go to ```ballerina-tools-0.8.0```.  
3 Create a directory ```mySample```.  
4 Create the following two files inside this directory.  

e.g.: sample.bal
```
package mySample;

import ballerina.lang.system;
 
function main (string[] args) {
    int i = intAdd(1, 2);
    system:println("Result: " + i);
}
 
function intAdd(int a, int b) (int) {
    return a + b;
}

```  
e.g.: sample_test.bal
```
package mySample;
 
import ballerina.test;
 
function testInt() {	
    int answer = 0;
    answer = intAdd(1, 2);
    test:assertEquals(answer, 3, "IntAdd function failed");
	
}
```
Note the package hierarchy in above files.   
 
5 Run tests using following command.  
```> ./bin/ballerina test mySample/```

### Running Samples

#### Running mock sample
- Download ```ballerina-tools-0.8.0.zip``` distribution and unzip.  
- Copy ```samples/mock directory``` to ```ballerina-tools-0.8.0```.  
- Run tests as follows.  
```> ./bin/ballerina test mock/```

### Reference:  
[1] https://github.com/ballerinalang/distribution