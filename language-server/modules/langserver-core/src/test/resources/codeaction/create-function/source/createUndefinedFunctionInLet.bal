import ballerina/module1;

type Car record {
    string licenceNo;
    string model;
    module1:TestRecord1 rec1;
};

function testCreateFunction() {
    string licenceNo = "1234";
    module1:TestClass1 tc = new();
    
    Car myCar = {
        licenceNo: let string year = getYear(tc) in licenceNo.concat("-", year),
        model: "Toyota",
        rec1: let int x = 3 in getTestRecord(x)
    };
    
    
}
