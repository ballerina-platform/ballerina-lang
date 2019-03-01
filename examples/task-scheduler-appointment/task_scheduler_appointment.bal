import ballerina/io;
import ballerina/task;
import ballerina/runtime;

int reminderCount = 0;

public function main () {
    // Appointment data record is used to provide appointment configurations.
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
    task:Scheduler appointment = new({ appointmentDetails: appointmentData });

    // Attach the service to the Scheduler.
    _ = appointment.attach(appointmentService);

    // Start the scheduler.
    _ = appointment.start();

    // Wait for sometime
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
        reminderCount = reminderCount + 1;
        io:println("Schedule is due - Reminder: " + reminderCount);
    }
};
