# Testerina

Testerina is the test framework built for the Ballerina language.

## How to run

This will be a part of Ballerina distribution.  
For the time being, you can copy the built ```testerina-core-0.8.0-SNAPSHOT.jar```  
file to ```<ballerina_home>/bre/lib``` and test.  

To test a file written in Ballerina language, go to the parent folder and use the test command as follows.

```./ballerina test <package name>```

Test files containing ```_test.bal``` suffix in the file name, will be selected and inside the ```*_test.bal``` file,
functions which has the suffix ```test``` will be invoked. 
Within the ```test*``` function you can use the following built in assert functions to assert your actual response with
the expected value. 

```assertEquals(actual, expected, message)```

```assertTrue(booleanCondtion, message)```

```assertFalse(booleanCondtion, message)```