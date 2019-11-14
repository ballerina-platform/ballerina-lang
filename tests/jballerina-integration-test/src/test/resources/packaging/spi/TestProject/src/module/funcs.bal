import ballerina/io;
import ballerinax/java;

public function main() {
    io:println(getDriversAsString());
}

public function  getDriversAsString() returns string = @java:Method {
      class:"PrintDrivers"
} external;

