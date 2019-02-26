import ballerina/io;
import ballerina/task;

boolean runSchedule = true;
int reminderCount = 0;
int maximumReminders = 5;

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
    _ = appointment.attachService(appointmentService);

    // Start the scheduler.
    _ = appointment.run();

    // Wait until an error is occurred.
    while (runSchedule) {

    }

    // Cancel the appointment.
    var result = appointment.cancel();
    if (result is error) {
        io:println("Error occurred while cancelling the task");
        return;
    }
    io:println("Appointment cancelled.");
}


// Creating a service on the task Listener.
service appointmentService = service {
    // This resource triggers when the appointment is due.
    resource function onTrigger() returns error? {
        reminderCount = reminderCount + 1;
        io:println("Schedule is due - Reminder: " + reminderCount);
        // Return an intentional error to demonstrate the error propagation.
        if (reminderCount == maximumReminders) {
            error e = error("Maximum reminder count reached.");
            return e;
        }
    }

    // This resource will trigger when an error is returned from the
    // onTrigger() resource.
    resource function onError(error e) {
        io:print("Error occurred while triggering the appointment: ");
        io:println(e.reason());
        runSchedule = false;
    }
};
