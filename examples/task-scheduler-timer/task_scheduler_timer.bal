import ballerina/io;
import ballerina/math;
import ballerina/task;

// Define a custom record type tyo use in timer.
public type Person record {
    string name;
    int age;
    int maxAge;
};

public function main() {
    // Interval in which the timer should trigger.
    int interval = 1000;

    // Initialize the timer scheduler using the interval value.
    // Delay will be equal to the interval as we do not mention the delay here.
    task:Scheduler timer = new({ interval: interval });

    // Define a person object
    Person person = { name: "Sam", age: 0, maxAge: 10 };

    // Attaching the service to the timer. This will not start the timer.
    // But it will attach the service to the timer, and also passes the
    // person object into the timer service.
    // Defaultable `serviceParameter` will pass the object into the resources
    // if it is set.
    _ = timer.attach(service1, serviceParameter = person);
    _ = timer.attach(service2, serviceParameter = person);

    // Start the timer.
    _ = timer.start();

    // While loop will stop function from exiting until the service ends.
    while (person.age < person.maxAge) {
        // Wait for sometime before stop the service.
    }

    // Cancel the timer. This will stop the timer and all the services
    // attached to it.
    _ = timer.stop();

    io:println("End.");
}

// Service which will be attached to the timer.
service service1 = service {
    // On trigger resource which will trigger when timer runs off.
    // Note the usage of Person object passing inside the function, which we
    // attached with the timer.
    resource function onTrigger(Person person) {
        person.age = person.age + 1;
        io:println("Hi " + person.name + " you are " + person.age + " years old now.");
    }
};

service service2 = service {
    resource function onTrigger(Person person) {
        if (person.age > 5) {
            io:println(person.name + " is started schooling at age " + person.age);
        }
    }
};
