package functionTest;


import ballerina.test;
import ballerina.io;

@test:beforeSuite
function beforeTest(){
	io:println("Running Test setup in beforeTest function");
}

@test:config
function testIntAddFunction () {	
    int answer = 0;
	answer = intAdd(3, 5);
        
    test:assertEquals(answer, 8, "testIntAdd fucntion failed");
}

@test:config
function testIntSubtractFunction () {	
    int answer1 = intSubtract(8, 5);    
	int answer2 = intSubtract(5, 8);          

	test:assertEquals(answer1, 3, "intSubstract fucntion failed");
    test:assertEquals(answer2, -3, "intSubstract function failed for minus value");
}

@test:config
function testfloatAdd(){
	float float1 = 10.000;
	float float2 = 20.050;
	float answer = floatAdd(float1, float2);

    test:assertEquals(answer, 30.050, "floatAdd fucntion failed");
}

@test:config
function testfloatSubtract(){
	float float1 = 10.000;
	float float2 = 20.050;

	float answer1 = floatSubtract(float1, float2);
	float answer2 = floatSubtract(float2, float1);

    test:assertEquals(answer1, -10.050, "floatSubstract fucntion failed for minus value");
	test:assertEquals(answer2, 10.050, "floatSubstract fucntion failed");
}

@test:config
function testStringConcat(){
	string name1 = "John";
	string name2 = "Doe";

	string concatenated = stringConcat(name1, name2);

    test:assertEquals(concatenated, "JohnDoe", "string concatenation failed");
}

@test:config
function testStringAndIntConcat(){
	string name = "John";
	int number = 18;

	string concatenated = stringAndIntConcat(name, number);

    test:assertEquals(concatenated, "John18", "string and int concatenation failed");
}

@test:afterSuite
function afterTest(){
	io:println("Finishing tests in afterTest function");
}
