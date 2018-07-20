import ballerina/io;

function testPrintingObject() returns Person {
	Person p = new(20, "John", 2018, "March");
	io:println(p);
	return p;
}

type Person object {
    public int age = 10,
    public string name = "sample name";

    int year = 50;
    string month = "February";
    
    new (age, name, year, month) {
    }
};
