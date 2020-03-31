import ballerina/io;
import ballerina/runtime;
import ballerina/task;

int reminderCount = 0;

public function main() {
    // The Appointment data record provides the appointment configurations.
    task:AppointmentData appointmentData = {
        seconds: "0/2",
        minutes: "*",
        hours: "*",
        daysOfMonth: "?",
        months: "*",
        daysOfWeek: "*",
        year: "*"
    };

    // Create an Appointment using the configurations.
    task:Scheduler appointment = new ({appointmentDetails: appointmentData});

    // Attach the service to the scheduler and exit if there is an error.
    var attachResult = appointment.attach(appointmentService);
    if (attachResult is error) {
        io:println("Error attaching the service.");
        return;
    }

    // Start the scheduler and exit if there is an error.
    var startResult = appointment.start();
    if (startResult is error) {
        io:println("Starting the task is failed.");
        return;
    }

    runtime:sleep(10000);

    // Cancel the appointment.
    var result = appointment.stop();
    if (result is error) {
        io:println("Error occurred while cancelling the task");
        return;
    }
    io:println("Appointment cancelled.");
}

// Creating a service on the task Listener.
service appointmentService = service {
    // This resource triggers when the appointment is due.
    resource function onTrigger() {
        if (reminderCount < 5) {
            reminderCount = reminderCount + 1;
            io:println("Schedule is due - Reminder: " + reminderCount.toString());
        }
    }
};
