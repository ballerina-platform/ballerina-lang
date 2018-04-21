
import ballerina/test;
import ballerina/io;

@test:BeforeSuite
function beforeTest(){
	io:println("Running Test setup in beforeTest function");
}

@test:Config
function testIntAddFunction () {	
    int answer = 0;
	answer = intAdd(3, 5);

    test:assertEquals(answer, 8, msg = "testIntAdd fucntion failed");
}

@test:Config
function testIntSubtractFunction () {	
    int answer1 = intSubtract(8, 5);    
	int answer2 = intSubtract(5, 8);          

	test:assertEquals(answer1, 3, msg = "intSubstract fucntion failed");
    test:assertEquals(answer2, -3, msg = "intSubstract function failed for minus value");
}

@test:Config
function testfloatAdd(){
	float float1 = 10.000;
	float float2 = 20.050;
	float answer = floatAdd(float1, float2);

    test:assertEquals(answer, 30.050, msg = "floatAdd fucntion failed");
}

@test:Config
function testfloatSubtract(){
	float float1 = 10.000;
	float float2 = 20.050;

	float answer1 = floatSubtract(float1, float2);
	float answer2 = floatSubtract(float2, float1);

    test:assertEquals(answer1, -10.050, msg = "floatSubstract fucntion failed for minus value");
	test:assertEquals(answer2, 10.050, msg = "floatSubstract fucntion failed");
}

@test:Config
function testStringConcat(){
	string name1 = "John";
	string name2 = "Doe";

	string concatenated = stringConcat(name1, name2);

    test:assertEquals(concatenated, "JohnDoe", msg = "string concatenation failed");
}

@test:Config
function testStringAndIntConcat(){
	string name = "John";
	int number = 18;

	string concatenated = stringAndIntConcat(name, number);

    test:assertEquals(concatenated, "John18", msg = "string and int concatenation failed");
}

@test:AfterSuite
function afterTest(){
	io:println("Finishing tests in afterTest function");
}
