import ballerina/log;
import ballerina/task;

// Task Appointment configuration record to task Listener.
// Task Appointment can have either a a cronExpression (`string`)
// or a `AppointmentData` record the `appointmentData` field.
// Optionally a `noOfRecurrences` can be provided to limit the number of runs
// an appointment should run.
task:AppointmentConfiguration appointmentConfiguration = {
    // This cron expression will schedule the appointment once every 2 seconds.
    appointmentDetails: "0/2 * * * * ?",
    // Number of recurrences will limit the number of times the timer runs.
    noOfRecurrences: 10
};

// Initialize the listener using pre defined configurations.
listener task:Listener appointment = new(appointmentConfiguration);

int count = 0;

// Creating a service on the task Listener.
service appointmentService on appointment {
    // This resource triggers when the appointment is due.
    resource function onTrigger() returns error? {
        count = count + 1;
        log:printInfo("Cleaning up...");
        log:printInfo(string.convert(count));

        // Returning an error to show the usage when an error is returned
        // from onTrigger() resource.
        if (count == 5) {
            error e = error("Count error.");
            return e;
        }
    }

    // This resource will trigger when an error is returned from the
    // onTrigger() resource.
    resource function onError(error e) {
        log:printError("Cleanup failed", err = e);
    }
}
