import ballerina/java;

function newVehicle(handle strName) returns handle = @java:Constructor {
    'class:"org.ballerinalang.test.javainterop.overloading.pkg.Vehicle",
    paramTypes:["java.lang.String"]
} external;

function newCar(handle strName, handle strModel) returns handle = @java:Constructor {
    'class:"org.ballerinalang.test.javainterop.overloading.pkg.Car"
} external;


function getName(handle receiver) returns handle = @java:Method {
    'class:"org.ballerinalang.test.javainterop.overloading.pkg.Car"
} external;

function getDescription(handle receiver, handle inputStr) returns handle = @java:Method {
    'class:"org.ballerinalang.test.javainterop.overloading.pkg.Car"
} external;


public function testOverriddenMethods() returns [string?, string?] {
    handle strName1 = java:fromString("Generic vehicle");
    handle vehicle = newVehicle(strName1);

    handle strName2 = java:fromString("Motor Car");
    handle strModel = java:fromString("Honda");
    handle car = newCar(strName2, strModel);

    handle carName = getName(car);
    handle carDesc = getDescription(car, java:fromString("Graze"));
    return [java:toString(carName), java:toString(carDesc)];
}
