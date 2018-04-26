import ballerina/task;
import ballerina/math;
import ballerina/log;
import ballerina/runtime;

int app1Count;
task:Appointment? appointment1;

function main(string... args) {
    worker w1 {
        log:printInfo("------ Scheduling Appointments --------------");

        (function() returns error?) onTriggerFunction = appointment1Cleanup;
        (function(error)) onErrorFunction = cleanupError;

        //job 1 runs every 20 seconds.
        appointment1 = new task:Appointment(onTriggerFunction, 
                                            onErrorFunction, 
                                            "0/20 * * * * ?");
                                            
        appointment1.schedule();

        //job 2 runs every other minute (at 15 seconds past the minute).
        onTriggerFunction = appointment2Cleanup;
        task:Appointment appointment2 = 
                  new task:Appointment(onTriggerFunction, 
                                       onErrorFunction, 
                                       "15 0/2 * * * ?");
                                       
        appointment2.schedule();

        //job 3 runs every other minute but only between 8am and 5pm.
        onTriggerFunction = appointment3Cleanup;
        task:Appointment appointment3 = 
                  new task:Appointment(onTriggerFunction, 
                                       onErrorFunction, 
                                       "0 0/2 8-17 * * ?");
                                       
        appointment3.schedule();

        //job 4 runs every three minutes but only between 5pm and 11pm.
        onTriggerFunction = appointment4Cleanup;
        task:Appointment appointment4 = 
                  new task:Appointment(onTriggerFunction, 
                                       onErrorFunction, 
                                       "0 0/3 17-23 * * ?");
                                       
        appointment4.schedule();

        //job 5 runs at 10am on the 1st and the 15th days of the month.
        onTriggerFunction = appointment5Cleanup;
        task:Appointment appointment5 = 
                  new task:Appointment(onTriggerFunction, 
                                       onErrorFunction, 
                                       "0 0 10am 1,15 * ?");
                                       
        appointment5.schedule();

        //job 6 runs every 30 seconds but only on weekdays
        // (i.e., Monday through Friday).
        onTriggerFunction = appointment6Cleanup;
        task:Appointment appointment6 = 
                  new task:Appointment(onTriggerFunction, 
                                       onErrorFunction, 
                                       "0,30 * * ? * MON-FRI");
                                       
        appointment6.schedule();

        //job 7 runs every 30 seconds but only on weekends
        // (i.e., Saturday and Sunday).
        onTriggerFunction = appointment7Cleanup;
        task:Appointment appointment7 = 
                  new task:Appointment(onTriggerFunction, 
                                       onErrorFunction, 
                                       "0,30 * * ? * SAT,SUN");
        
        appointment7.schedule();

        runtime:sleep(600000); // Temp workaround to stop the process from exiting.
    }
}

function appointment1Cleanup() returns (error?) {
    log:printInfo("Appointment#1 cleanup running...");
    app1Count = app1Count + 1;
    if (app1Count == 5) {
        log:printInfo("Stopping Appointment#1 cleanup task since it
                       has run 5 times");
        
        // This is how you cancel an appointment.
        appointment1.cancel();
    }
    return cleanup();
}

function appointment2Cleanup() returns (error?) {
    log:printInfo("Appointment#2 cleanup running...");
    return cleanup();
}

function appointment3Cleanup() returns (error?) {
    log:printInfo("Appointment#3 cleanup running...");
    return cleanup();
}

function appointment4Cleanup() returns (error?) {
    log:printInfo("Appointment#4 cleanup running...");
    return cleanup();
}

function appointment5Cleanup() returns (error?) {
    log:printInfo("Appointment#5 cleanup running...");
    return cleanup();
}

function appointment6Cleanup() returns (error?) {
    log:printInfo("Appointment#6 cleanup running...");
    return cleanup();
}

function appointment7Cleanup() returns (error?) {
    log:printInfo("Appointment#7 cleanup running...");
    return cleanup();
}

function cleanup() returns (error?) {
    log:printInfo("Cleaning up");
    if (math:randomInRange(0, 10) == 5) {
        error e = { message: "Cleanup error" };
        return e;
    }
    return ();
}

function cleanupError(error e) {
    log:printError("[ERROR] cleanup failed", err = e);
}
