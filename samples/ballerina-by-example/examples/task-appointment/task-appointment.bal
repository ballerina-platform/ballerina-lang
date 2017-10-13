import ballerina.lang.files;
import ballerina.lang.system;
import ballerina.task;
import ballerina.utils.logger;

function main (string[] args) {
    int minute;
    int hour;
    int dayOfWeek;
    int dayOfMonth;
    int month;
    any e1;
    any e2;
    any e3;
    any e4;
    any e5;
    int appointmentSchedulerTaskId;
    any appointmentSchedulerError;
    if (args[0] == "null") {
        minute = -1;
    } else {
        minute, e1 = <int>args[0];
    }
    if (args[1] == "null") {
        hour = -1;
    } else {
        hour, e2 = <int>args[1];
    }
    if (args[2] == "null") {
        dayOfWeek = -1;
    } else {
        dayOfWeek, e3 = <int>args[2];
    }
    if (args[3] == "null") {
        dayOfMonth = -1;
    } else {
        dayOfMonth, e4 = <int>args[3];
    }
    if (args[4] == "null") {
        month = -1;
    } else {
        month, e5 = <int>args[4];
    }
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

    system:sleep(600000);
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
