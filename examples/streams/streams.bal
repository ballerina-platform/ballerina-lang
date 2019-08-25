import ballerina/io;
import ballerina/runtime;

type Employee record {
    int id;
    string name;
};

public function main() {
    // Defines a `stream`, which is constrained by the `Employee` type.
    stream<Employee> employeeStream = new;

    // Subscribes to the `employeeStream` using a function that accepts `Employee` values.
    employeeStream.subscribe(printEmployeeName);

    // Publishes `Employee` values to the `stream`.
    Employee e1 = { id: 1, name: "Jane" };
    Employee e2 = { id: 2, name: "Anne" };
    Employee e3 = { id: 3, name: "John" };

    employeeStream.publish(e1);
    employeeStream.publish(e2);
    employeeStream.publish(e3);

    // Allows receipt by subscribers. The `printEmployeeName()` function should be invoked for each published value.
    runtime:sleep(1000);


    // Defines a stream, which is constrained by the `float` type.
    stream<float> temperatureStream = new;

    // Subscribes to the `temperatureStream` using a function that accepts `float` values.
    temperatureStream.subscribe(printTemperature);

    // Publishes `float` values to the stream indicating the temperature.
    temperatureStream.publish(28.0);
    temperatureStream.publish(30.1);
    temperatureStream.publish(29.5);

    // Waits for the subscriber to receive the values. The `printTemperature()` function 
    // should be invoked for each published value.
    runtime:sleep(1000);


    // Defines a `stream`, which accepts values of the `anydata` type.
    stream<anydata> updateStream = new;

    // Subscribes to the `stream` using a function that accepts values of the `anydata` type.
    updateStream.subscribe(printEvent);

    // Publishes values to the `stream`.
    updateStream.publish("Hello Ballerina!");
    updateStream.publish(1.0);
    updateStream.publish(e1);

    // Wait for the subscriber to receive the values. The `printEvent()` function should be invoked for each published
    // value.
    runtime:sleep(1000);
}

// This function accepts `Employee` values and is used to subscribe to a stream.
function printEmployeeName(Employee employee) {
    io:println("Employee event received for Employee Name: ",
        employee.name);
}

// This function accepts `float` values and is used to subscribe to a stream.
function printTemperature(float temperature) {
    io:println("Temperature event received: ",
        temperature.toString());
}

// This function accepts values of `anydata` type and is used to subscribe to a stream.
function printEvent(anydata event) {
    io:println("Event received: ", event);
}
