import ballerina/log;
import ballerina/task;

// Task Appointment configuration record of the Task Listener.
// Task Appointment can either have a CRON expression (`string`) or an
// `AppointmentData` record for the `appointmentData` field. Optionally, a
// `noOfRecurrences` can be provided to limit the number of executions.
task:AppointmentConfiguration appointmentConfiguration = {
    // This cron expression will schedule the appointment once every 2 seconds.
    appointmentDetails: "0/2 * * * * ?",
    // Number of recurrences will limit the number of times the timer runs.
    noOfRecurrences: 11
};

// Initialize the listener using pre defined configurations.
listener task:Listener appointment = new(appointmentConfiguration);

int count = 0;

// Creating a service on the task Listener.
service appointmentService on appointment {
    // This resource triggers when the appointment is due.
    resource function onTrigger() {
        count = count + 1;
        log:printInfo("Cleaning up...");
        log:printInfo(count.toString());
    }
}
