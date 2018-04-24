import ballerina/io;
import ballerina/runtime;

type Employee {
    int id,
    string name,
};

function main(string... args) {

    // Declare a stream constrained by the Employee type.
    stream<Employee> employeeStream;

    // Subscribe to the employee stream with a function accepting Employee events
    employeeStream.subscribe(printEmployeeName);

    // Publish Employee events to the stream
    Employee e1 = { id: 1, name: "Jane" };
    Employee e2 = { id: 2, name: "Anne" };
    Employee e3 = { id: 3, name: "John" };

    employeeStream.publish(e1);
    employeeStream.publish(e2);
    employeeStream.publish(e3);

    // Allow for receipt by subscribers. The printEmployeeName function would be invoked for each published event.
    runtime:sleep(1000);


    // Declare a stream constrained by float values.
    stream<float> temperatureStream;

    // Subscribe to the temperature stream with a function accepting float events.
    temperatureStream.subscribe(printTemperature);

    // Publish float events to the stream, indicating temperature
    temperatureStream.publish(28.0);
    temperatureStream.publish(30.1);
    temperatureStream.publish(29.5);

    // Allow for receipt by subscribers. The printTemperature function would be invoked for each published event.
    runtime:sleep(1000);


    // Declare an unconstrained stream which would accept events of any type, equivalent to stream<any>.
    stream updateStream;

    // Subscribe to the unconstrained stream with a function accepting events of any type.
    updateStream.subscribe(printEvent);

    // Publish events to the stream.
    updateStream.publish("Hello Ballerina!");
    updateStream.publish(1.0);
    updateStream.publish(e1);

    // Allow for receipt by subscribers. The printEvent function would be invoked for each published event.
    runtime:sleep(1000);

}

// Function used to subscribe to a stream accepting employee events.
function printEmployeeName(Employee employee) {
    io:println("Employee event received for Employee Name: " + employee.name);
}

// Function used to subscribe to a stream accepting float events.
function printTemperature(float temperature) {
    io:println("Temperature event received: " + temperature);
}

// Function used to subscribe to a stream accepting events of any type.
function printEvent(any event) {
    io:println("Event received: ", event);
}
