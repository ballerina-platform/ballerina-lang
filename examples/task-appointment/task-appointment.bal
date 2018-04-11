import ballerina/task;
import ballerina/math;
import ballerina/log;

int app1Count;
string app1Tid;

function main (string[] args) {
    worker w1 {
        log:printInfo("------- Scheduling Appointments ----------------");

        function () returns (error|()) onTriggerFunction =  () => (error|()) { return ();};
        function (error e) onErrorFunction = (error e) => () {};

        // job 1 will run every 20 seconds
        onTriggerFunction = appointment1Cleanup;
        onErrorFunction = cleanupError;
        app1Tid = task:scheduleAppointment(onTriggerFunction, onErrorFunction,
                                            "0/20 * * * * ?");

        // job 2 will run every other minute (at 15 seconds past the minute)
        onTriggerFunction = appointment2Cleanup;
        onErrorFunction = cleanupError;
        _ = task:scheduleAppointment(onTriggerFunction, onErrorFunction,
                                     "15 0/2 * * * ?");

        // job 3 will run every other minute but only between 8am and 5pm.
        onTriggerFunction = appointment3Cleanup;
        onErrorFunction = cleanupError;
        _ = task:scheduleAppointment(onTriggerFunction, onErrorFunction,
                                     "0 0/2 8-17 * * ?");

        // job 4 will run every three minutes but only between 5pm and 11pm.
        onTriggerFunction = appointment4Cleanup;
        onErrorFunction = cleanupError;
        _ = task:scheduleAppointment(onTriggerFunction, onErrorFunction,
                                     "0 0/3 17-23 * * ?");

        // job 5 will run at 10am on the 1st and 15th days of the month.
        onTriggerFunction = appointment5Cleanup;
        onErrorFunction = cleanupError;
        _ = task:scheduleAppointment(onTriggerFunction, onErrorFunction,
                                     "0 0 10am 1,15 * ?");

        // job 6 will run every 30 seconds but only on Weekdays. (Monday through Friday)
        onTriggerFunction = appointment6Cleanup;
        onErrorFunction = cleanupError;
        _ = task:scheduleAppointment(onTriggerFunction, onErrorFunction,
                                     "0,30 * * ? * MON-FRI");

        // job 7 will run every 30 seconds but only on Weekends. (Saturday and Sunday)
        onTriggerFunction = appointment7Cleanup;
        onErrorFunction = cleanupError;
        _ = task:scheduleAppointment(onTriggerFunction, onErrorFunction,
                                     "0,30 * * ? * SAT,SUN");
    }
}

function appointment1Cleanup () returns (error|()) {
    log:printInfo("Appointment#1 cleanup running...");
    app1Count = app1Count + 1;
    if (app1Count == 5) {
        log:printInfo("Stopping Appointment#1 cleanup task since it has run 5 times");
        // This is how you stop a task.
        _ = task:stopTask(app1Tid);
    }
    return cleanup();
}

function appointment2Cleanup () returns (error|()) {
    log:printInfo("Appointment#2 cleanup running...");
    return cleanup();
}

function appointment3Cleanup () returns (error|()) {
    log:printInfo("Appointment#3 cleanup running...");
    return cleanup();
}

function appointment4Cleanup () returns (error|()) {
    log:printInfo("Appointment#4 cleanup running...");
    return cleanup();
}

function appointment5Cleanup () returns (error|()) {
    log:printInfo("Appointment#5 cleanup running...");
    return cleanup();
}

function appointment6Cleanup () returns (error|()) {
    log:printInfo("Appointment#6 cleanup running...");
    return cleanup();
}

function appointment7Cleanup () returns (error|()) {
    log:printInfo("Appointment#7 cleanup running...");
    return cleanup();
}

function cleanup () returns (error|()) {
    log:printInfo("Cleaning up");
    if (math:randomInRange(0, 10) == 5) {
        error e = {message:"Cleanup error"};
        throw e;
    }
    return ();
}

function cleanupError (error e) {
    log:printErrorCause("[ERROR] cleanup failed", e);
}
