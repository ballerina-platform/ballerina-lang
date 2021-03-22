import ballerina/module1;

type Manufacturer record {
    string name;
};

type Car record {
    string licenceNo;
    Manufacturer manufacturer;
};

type MyListener record {
    module1:Listener myListener;
};

function testCreateFunction() {
    Car myCar = {
        licenceNo: "1234",
        manufacturer: getManufacturer()
    };
    
    MyListener ml = {
        myListener: createListener(myCar)
    };
}
