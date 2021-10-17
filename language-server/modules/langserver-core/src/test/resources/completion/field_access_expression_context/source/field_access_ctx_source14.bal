import ballerina/module1;

class Car {
    
    string brandName;
    string modelName;
    int manufacturedYear;
    string owner;
    
    function getModel() returns string {
        return '${self.brandName} ${self.modelName} - ${self.manufacturedYear}';
    }
}

function testObjectFieldAccess() {
    Car myCar = new;
    myCar.
}
