import ballerina.lang.files;
import ballerina.lang.system;
import ballerina.task;
import ballerina.utils.logger;

function scheduleAppointment (int minute, int hour, int dayOfWeek, int dayOfMonth, int month, int sleepInterval) returns (int) {
    int appointmentSchedulerTaskId = -1;
    any appointmentSchedulerError;
    task:AppointmentScheduler aScheduler = {minute:minute, hour:hour, dayOfWeek:dayOfWeek, dayOfMonth:dayOfMonth, month:month};

    function () returns (any) scheduleAppointmentOnTriggerFunction;
    scheduleAppointmentOnTriggerFunction = cleanup;
    function (any) scheduleAppointmentOnErrorFunction;
    scheduleAppointmentOnErrorFunction = cleanupError;

    appointmentSchedulerTaskId, appointmentSchedulerError = task:scheduleAppointment(scheduleAppointmentOnTriggerFunction, scheduleAppointmentOnErrorFunction, aScheduler);
    var appointmentSchedulerErrorMessage, castErrorAS = (string)appointmentSchedulerError;
    if(appointmentSchedulerErrorMessage != "") {
        logger:error("Appointment scheduling failed: " + appointmentSchedulerErrorMessage);
    }

    system:sleep(sleepInterval);

    return appointmentSchedulerTaskId;
}

function cleanup () returns (any) {
    files:File targetDir = {path:"/tmp/tmpDir"};
    files:delete(targetDir);
    boolean b = files:exists(targetDir);
    if (!b) {
        logger:info("Temporary directory /tmp/tmpDir is cleaned up");
        return "SUCCESS";
    }
    return "";
}

function cleanupError (any error) {
    var errorMessage, castErr = (string)error;
    if (errorMessage != "") {
        logger:error("Error while cleaning up the tmp directory: " + errorMessage);
    }
}
