import ballerina/task;

int count;
task:Appointment? app;

function scheduleAppointment(string cronExpression) {
    (function() returns error?) onTriggerFunction = onTrigger;
    (function (error)) onErrorFunction = cleanupError;
    app = new task:Appointment(onTriggerFunction, onErrorFunction, cronExpression);
    _ = app.schedule();
}

function getCount() returns (int) {
    return count;
}

function onTrigger() returns error? {
    count = count + 1;
    return ();
}

function cleanupError(error e) {

}

function cancelAppointment() {
    _ = app.cancel();
    count = -1;
}
