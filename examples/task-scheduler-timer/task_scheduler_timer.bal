import ballerina/io;
import ballerina/runtime;
import ballerina/task;

// Define a custom record type to use in timer.
public type Person record {|
    string name;
    int age;
    int maxAge;
|};

public function main() {
    // Interval in which the timer should trigger.
    int interval = 1000;

    // Initializes the timer scheduler using the interval value.
    // The delay will be equal to the interval as we do not provide an initial delay here.
    task:Scheduler timer = new({ interval: interval });

    // Define a person object
    Person person = { name: "Sam", age: 0, maxAge: 10 };

    // Attaching the service to the timer. This will not start the timer.
    // But it will attach the service to the timer, and also passes the
    // person object into the timer service.
    // Defaultable `serviceParameter` will pass the object into the resources
    // if it is set.
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

    // Start the timer.
    var startResult = timer.start();
    if (startResult is error) {
        io:println("Starting the task is failed.");
        return;
    }

    // While loop will stop function from exiting until the service ends.
    while (person.age < person.maxAge) {
        // Wait until person age reaches max age.
    }

    // Additional sleep to finish onTrigger function.
    runtime:sleep(1000);

    // Cancel the timer. This will stop the timer and all the services
    // attached to it.
    var stopResult = timer.stop();
    if (stopResult is error) {
        io:println("Stopping the task is failed.");
        return;
    }

    io:println("End.");
}

// Service which will be attached to the timer.
service service1 = service {
    // onTrigger resource which will trigger when the timer runs off.
    // Note the usage of Person object passing inside the function, which we
    // attached with the timer.
    resource function onTrigger(Person person) {
        if (person.age < person.maxAge) {
            person.age = person.age + 1;
            io:println("Hi " + person.name + " you are " + person.age + " years old now.");
        }
    }
};

service service2 = service {
    resource function onTrigger(Person person) {
        if (person.age == 5) {
            io:println(person.name + " started schooling at age " + person.age);
        }
    }
};
