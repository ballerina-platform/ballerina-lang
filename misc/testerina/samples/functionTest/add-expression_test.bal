package samples.functionTest;


import ballerina/test;
import ballerina/lang.system;

function beforeTest(){
	system:println("Running Test setup in beforeTest function");
}

function testIntAddFunction () {	
    int answer = 0;
	answer = intAdd(3, 5);
        
    test:assertIntEquals(answer, 8, "testIntAdd fucntion failed");
}

function testIntSubtractFunction () {	
    int answer1 = intSubtract(8, 5);    
	int answer2 = intSubtract(5, 8);          

	test:assertIntEquals(answer1, 3, "intSubstract fucntion failed");
    test:assertIntEquals(answer2, -3, "intSubstract function failed for minus value");
}

function testfloatAdd(){
	float float1 = 10.000;
	float float2 = 20.050;
	float answer = floatAdd(float1, float2);

    test:assertFloatEquals(answer, 30.050, "floatAdd fucntion failed");	
}

function testfloatSubtract(){
	float float1 = 10.000;
	float float2 = 20.050;

	float answer1 = floatSubtract(float1, float2);
	float answer2 = floatSubtract(float2, float1);

    test:assertFloatEquals(answer1, -10.050, "floatSubstract fucntion failed for minus value");
	test:assertFloatEquals(answer2, 10.050, "floatSubstract fucntion failed");
}

function testStringConcat(){
	string name1 = "John";
	string name2 = "Doe";

	string concatenated = stringConcat(name1, name2);

    test:assertStringEquals(concatenated, "JohnDoe", "string concatenation failed");	
}


function testStringAndIntConcat(){
	string name = "John";
	int number = 18;

	string concatenated = stringAndIntConcat(name, number);

    test:assertStringEquals(concatenated, "John18", "string and int concatenation failed");	
}

function afterTest(){
	system:println("Finishing tests in afterTest function");
}
