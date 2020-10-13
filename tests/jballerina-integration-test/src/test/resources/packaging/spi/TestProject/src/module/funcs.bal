import ballerina/java;

public function main() {

}

public function getDriversAsString() returns handle = @java:Method {
      'class:"PrintDrivers"
} external;
