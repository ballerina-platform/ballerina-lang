import ballerina.lang.files;
import ballerina.task;
import ballerina.utils.logger;

function scheduleAppointment (int minute, int hour, int dayOfWeek, int dayOfMonth, int month, int sleepInterval) returns (int) {
    int appointmentSchedulerTaskId = -1;
    any appointmentSchedulerError;
    task:AppointmentScheduler aScheduler = {minute:minute, hour:hour, dayOfWeek:dayOfWeek, dayOfMonth:dayOfMonth, month:month};

    function () returns (error) scheduleAppointmentOnTriggerFunction;
    scheduleAppointmentOnTriggerFunction = cleanup;
    function (error) scheduleAppointmentOnErrorFunction;
    scheduleAppointmentOnErrorFunction = cleanupError;

    appointmentSchedulerTaskId, appointmentSchedulerError = task:scheduleAppointment(scheduleAppointmentOnTriggerFunction, scheduleAppointmentOnErrorFunction, aScheduler);
    var appointmentSchedulerErrorMessage, castErrorAS = (string)appointmentSchedulerError;
    if(appointmentSchedulerErrorMessage != "") {
        logger:error("Appointment scheduling failed: " + appointmentSchedulerErrorMessage);
    }

    sleep(sleepInterval);

    return appointmentSchedulerTaskId;
}

function cleanup () returns (error) {
    files:File targetDir = {path:"/tmp/tmpDir"};
    files:delete(targetDir);
    boolean b = files:exists(targetDir);
    if (!b) {
        logger:info("Temporary directory /tmp/tmpDir is cleaned up");
    } else {
        error err = {msg:"Unable to clean up the tmp directory"};
        return err;
    }
    return null;
}

function cleanupError (error error) {
    if (error != null) {
        logger:error("Error: " + error.msg);
    }
}
