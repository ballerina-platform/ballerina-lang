import ballerina/io;
import ballerinax/java;

public function main() {
    io:println(getDriversAsString());
}

public function  getDriversAsString() returns handle = @java:Method {
      class:"PrintDrivers"
} external;

