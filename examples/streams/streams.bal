import ballerina/io;
import ballerina/runtime;

type Employee record {
    int id,
    string name,
};

function main(string... args) {

    // Declare a stream constrained by the `Employee` type.
    stream<Employee> employeeStream;

    // Subscribe to the `employeeStream` using a function that accepts `Employee` events.
    employeeStream.subscribe(printEmployeeName);

    // Publish `Employee` events to the stream.
    Employee e1 = { id: 1, name: "Jane" };
    Employee e2 = { id: 2, name: "Anne" };
    Employee e3 = { id: 3, name: "John" };

    employeeStream.publish(e1);
    employeeStream.publish(e2);
    employeeStream.publish(e3);

    // Allow for receipt by subscribers. The `printEmployeeName` function should be invoked for each published event.
    runtime:sleep(1000);


    // Declare a stream constrained by `float` values.
    stream<float> temperatureStream;

    // Subscribe to the `temperatureStream` using a function that accepts `float` events.
    temperatureStream.subscribe(printTemperature);

    // Publish `float` events to the stream, indicating temperature.
    temperatureStream.publish(28.0);
    temperatureStream.publish(30.1);
    temperatureStream.publish(29.5);

    // Wait for the subscriber to receive the events. The `printTemperature` function should be invoked for each published event. 
    runtime:sleep(1000);


    // Declare an unconstrained stream that accepts events of any type, equivalent to `stream<any>`.
    stream updateStream;

    // Subscribe to the unconstrained stream using a function that accepts events of any type.
    updateStream.subscribe(printEvent);

    // Publish events to the stream.
    updateStream.publish("Hello Ballerina!");
    updateStream.publish(1.0);
    updateStream.publish(e1);

    // Wait for the subscriber to receive the events. The `printEvent` function should be invoked for each published event. 
    runtime:sleep(1000);

}

// This function accepts `Employee` events and is used to subscribe to a stream. 
function printEmployeeName(Employee employee) {
    io:println("Employee event received for Employee Name: "
                    + employee.name);
}

// This function accepts `float` events and is used to subscribe to a stream. 
function printTemperature(float temperature) {
    io:println("Temperature event received: " + temperature);
}

// This function accepts events of any type and is used to subscribe to a stream.  
function printEvent(any event) {
    io:println("Event received: ", event);
}
