import ballerina/io;
import ballerina/runtime;
import ballerina/task;

// Defines a custom record type to use in the timer.
public type Person record {|
    string name;
    int age;
    int maxAge;
|};

public function main() {
    // The interval in which the timer should trigger.
    int intervalInMillis = 1000;

    // Initializes the timer scheduler using the interval value.
    // The delay will be equal to the interval as an initial delay is not provided.
    task:Scheduler timer = new ({
        intervalInMillis: intervalInMillis,
        initialDelayInMillis: 0
    });

    // Define a person object
    Person person = {name: "Sam", age: 0, maxAge: 10};

    // Attaching the service to the timer. This will not start the timer.
    // However, it will attach the service to the timer and also passes the
    // person object into the `onTrigger()` resource
    var attachResult = timer.attach(service1, attachment = person);
    if (attachResult is error) {
        io:println("Error attaching the service1.");
        return;
    }

    attachResult = timer.attach(service2, attachment = person);
    if (attachResult is error) {
        io:println("Error attaching the service2.");
        return;
    }

    // Starts the timer.
    var startResult = timer.start();
    if (startResult is error) {
        io:println("Starting the task is failed.");
        return;
    }

    // While loop will stop the function from exiting until the service ends.
    while (person.age < person.maxAge) {
        // Waits until the age of the person reaches the max age.
        runtime:sleep(2000);
    }

    runtime:sleep(1000);

    // Cancels the timer. This will stop the timer and all the services
    // attached to it.
    var stopResult = timer.stop();
    if (stopResult is error) {
        io:println("Stopping the task is failed.");
        return;
    }

    io:println("End.");
}

// The service, which will be attached to the timer.
service service1 = service {
    // The onTrigger resource, which will trigger when the timer runs off.
    // The usage of the Person object being passed inside the function, which we
    // attached with the timer.
    resource function onTrigger(Person person) {
        if (person.age < person.maxAge) {
            person.age = person.age + 1;
            io:println("Hi " + person.name + " you are " + person.age.toString() + " years old now.");
        }
    }
};

service service2 = service {
    resource function onTrigger(Person person) {
        if (person.age == 5) {
            io:println(person.name + " started schooling");
        }
    }
};
