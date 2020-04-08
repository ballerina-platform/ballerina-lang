import ballerina/io;
import ballerina/java;

public function main() {
    io:println(getDriversAsString());
}

public function  getDriversAsString() returns handle = @java:Method {
      class:"PrintDrivers"
} external;

