import ballerina/jballerina.java;
import ballerina/test;

function newVehicle(handle strName) returns handle = @java:Constructor {
    'class:"org.ballerinalang.test.javainterop.overloading.pkg.Vehicle",
    paramTypes:["java.lang.String"]
} external;

function newCar(handle strName, handle strModel) returns handle = @java:Constructor {
    'class:"org.ballerinalang.test.javainterop.overloading.pkg.Car"
} external;

function newSportsCar(handle strName, handle strModel, int seatCount) returns handle = @java:Constructor {
    'class:"org.ballerinalang.test.javainterop.overloading.pkg.SportsCar"
} external;


function getName(handle receiver) returns handle = @java:Method {
    'class:"org.ballerinalang.test.javainterop.overloading.pkg.Car"
} external;

function getDescription(handle receiver, handle inputStr) returns handle = @java:Method {
    'class:"org.ballerinalang.test.javainterop.overloading.pkg.Car"
} external;

function getSeatCount(handle receiver) returns int = @java:Method {
    'class: "org.ballerinalang.test.javainterop.overloading.pkg.SportsCar",
    paramTypes: []
} external;

function getSeatCountWithModel(handle receiver) returns int = @java:Method {
    name: "getSeatCount",
    'class: "org.ballerinalang.test.javainterop.overloading.pkg.SportsCar",
    paramTypes: ["java.lang.String"]
} external;

function getSportsCarMaxSpeed(handle model) returns handle = @java:Method {
    name: "getMaxSpeed",
    'class: "org.ballerinalang.test.javainterop.overloading.pkg.SportsCar"
} external;

function getCarMaxSpeed(handle model) returns handle = @java:Method {
    name: "getMaxSpeed",
    'class: "org.ballerinalang.test.javainterop.overloading.pkg.Car"
} external;

function getMillage(int val) returns handle = @java:Method {
    'class: "org.ballerinalang.test.javainterop.overloading.pkg.SportsCar"
} external;

public function testOverriddenMethods() {
    handle strName1 = java:fromString("Generic vehicle");
    handle vehicle = newVehicle(strName1);

    handle strName2 = java:fromString("Motor Car");
    handle strModel = java:fromString("Honda");
    handle car = newCar(strName2, strModel);

    handle strName3 = java:fromString("Sports Car");
    handle strModel2 = java:fromString("Nissan-GTR");
    handle sportsCar = newSportsCar(strName3, strModel2, 4);

    handle carName = getName(car);
    handle carDesc = getDescription(car, java:fromString("Graze"));

    handle sportsCarMaxSpeed = getSportsCarMaxSpeed(java:fromString("BMW-S6"));
    handle carMaxSpeed = getCarMaxSpeed(java:fromString("BMW"));

    test:assertEquals(java:toString(carName), "Motor Car : Honda");
    test:assertEquals(java:toString(carDesc), "GrazeHonda");
    test:assertEquals(getSeatCount(sportsCar), 4);
    test:assertEquals(getSeatCountWithModel(java:fromString("Mazda MX-5")), 2);
    test:assertEquals(java:toString(sportsCarMaxSpeed), "250MPH");
    test:assertEquals(java:toString(carMaxSpeed), "200MPH");
    test:assertEquals(java:toString(getMillage(40)), "40MPG");
}
