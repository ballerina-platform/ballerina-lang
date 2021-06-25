import ballerina/io;
import ballerina/lang.runtime;
import ballerina/task;

int reminderCount = 0;

public function main() returns error? {

    // The [`task:AppointmentConfiguration`](https://ballerina.io/swan-lake/learn/api-docs/ballerina/#/ballerina/task/latest/task/records/AppointmentConfiguration) record of the task scheduler.
    task:AppointmentConfiguration appointmentConfiguration = {
        // This CRON expression will schedule the appointment every two second.
        cronExpression: "0/2 * * ? * * *"
    };

    // Creates an appointment using the given configuration.
    task:Scheduler appointment = check new (appointmentConfiguration);

    // Attaches the service to the scheduler.
    check appointment.attach(appointmentService);

    // Starts the scheduler.
    check appointment.start();

    runtime:sleep(9);

    // Cancels the appointment.
    check appointment.stop();

    io:println("Appointment cancelled.");
}

// Creating a service on the task listener.
service object {} appointmentService = service object {
    // This resource is triggered when the appointment is due.
    remote function onTrigger() {
        reminderCount += 1;
        io:println("Schedule is due - Reminder: ", reminderCount);
    }

};
