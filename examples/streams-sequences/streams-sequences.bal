import ballerina/runtime;
import ballerina/io;

// type representing the device temperature reading
type DeviceTempInfo {
    int deviceID;
    int roomNo;
    float temp;
};

// type representing the inital temperature and the peak temperature
type TempDiffInfo {
    float initialTemp;
    float peakTemp;
};

// input temperature readings
stream<DeviceTempInfo> tempStream;

// output stream with peak temperature values
stream<TempDiffInfo> tempDiffInfoStream;

TempDiffInfo[] tempDiffInfoArray = [];
int index;

// function which contains the rules which detect the temperature peak values
function deployPeakTempDetectionRules() {
    forever {
        from every tempStream as e1, tempStream where e1.temp <= temp [1..] as e2,
        tempStream where e2[e2.length-1].temp > temp as e3
        select e1.temp as initialTemp, e2[e2.length-1].temp as peakTemp
        => (TempDiffInfo[] tempDiffInfos) {
            // if the sequence is matched the data is pushed/published to output stream
            tempDiffInfoStream.publish(tempDiffInfos);
        }
    }
}

function main(string... args) {

    index = 0;
    // deploy the streaming sequence rules
    deployPeakTempDetectionRules();

    //subscribe to the function 'printInitialAndPeakTemp', so we can print whenver a peak temp is detected
    tempDiffInfoStream.subscribe(printInitalAndPeakTemp);

    DeviceTempInfo t1 = {deviceID:1, roomNo:23, temp:20.0};
    DeviceTempInfo t2 = {deviceID:1, roomNo:23, temp:22.5};
    DeviceTempInfo t3 = {deviceID:1, roomNo:23, temp:23.0};
    DeviceTempInfo t4 = {deviceID:1, roomNo:23, temp:21.0};
    DeviceTempInfo t5 = {deviceID:1, roomNo:23, temp:24.0};
    DeviceTempInfo t6 = {deviceID:1, roomNo:23, temp:23.9};

    // start simulating the events with temperature readings
    tempStream.publish(t1);
    runtime:sleepCurrentWorker(200);

    tempStream.publish(t2);
    runtime:sleepCurrentWorker(200);

    tempStream.publish(t3);
    runtime:sleepCurrentWorker(200);

    tempStream.publish(t4);
    runtime:sleepCurrentWorker(200);

    tempStream.publish(t5);
    runtime:sleepCurrentWorker(200);

    tempStream.publish(t6);

    // done sending 6 temperature events

    // wait till we collect the results for some sensible time.
    int count = 0;
    while(true) {
        runtime:sleepCurrentWorker(500);
        count++;
        if((lengthof tempDiffInfoArray) > 1 || count == 10) {
            break;
        }
    }
}

// function which prints the peak temperature readings
function printInitalAndPeakTemp(TempDiffInfo tempDiff) {
    io:println("printInitalAndPeakTemp function is invoked. InitialTemp:" + tempDiff.initialTemp + " and Peak temp :" +
            + tempDiff.peakTemp);
    addToGlobalTempDiffArray(tempDiff);
}

// this function is only used to keep track of all output temperature peak values.
function addToGlobalTempDiffArray(TempDiffInfo s) {
    tempDiffInfoArray[index] = s;
    index = index + 1;
}

