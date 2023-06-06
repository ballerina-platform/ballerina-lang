import ballerina/constraint;

@constraint:String {pattern: re `[A-Z]{1}`}
public type FlightClass string;

@constraint:String {pattern: re `[a-zA-Z0-9]{3}`}
public type Code string;

@constraint:String {pattern: re `[A-Z]{1,3}`}
public type FlightClass2 string;

@constraint:String {pattern: re `[a-zA-Z0-9]{3,5}`}
public type Code2 string;

@constraint:String {pattern: re `[A-Z]{1,}`}
public type FlightClass3 string;

@constraint:String {pattern: re `[a-zA-Z0-9]{3,}`}
public type Code3 string;

@constraint:String {pattern: re `[A-Z]{1,}`}
public type FlightClass4 string;

@constraint:String {pattern: re `[a-zA-Z0-9]{3,}`}
public type Code4 string;

public function main() {
    Code code = "A380";
    FlightClass flightclass = "A";

    Code2 code2 = "A380";
    FlightClass2 flightclass2 = "A";

    Code3 code3 = "A380";
    FlightClass3 flightclass3 = "A";

    Code4 code4 = "A380";
    FlightClass4 flightclass4 = "A";
}
