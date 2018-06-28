import ballerina/runtime;
import ballerina/io;

// Create a record type that represents the device temperature reading.
type DeviceTempInfo record {
    int deviceID;
    int roomNo;
    float temp;
};

// Create a record type that represents the initial temperature and the peak temperature.
type TempDiffInfo record {
    float initialTemp;
    float peakTemp;
};

// The stream that gets the input temperature readings.
stream<DeviceTempInfo> tempStream;

// The output stream with peak temperature values.
stream<TempDiffInfo> tempDiffInfoStream;

TempDiffInfo[] tempDiffInfoArray = [];
int index;

// This is the function that contains the rules that detect the temperature peak values. The first event's temperature
// should be greater than the temperature values that are returned with the next event, which is e2. The last
// temperature value in e2 should be greater than the temperature value that is given in the event e3. This makes
// the last value of e2, the peak temperature.
function deployPeakTempDetectionRules() {
    forever {
        from every tempStream as e1, tempStream
        where e1.temp <= temp [1..] as e2,
        tempStream where e2[e2.length - 1].temp > temp as e3
        select e1.temp as initialTemp,
        e2[e2.length - 1].temp as peakTemp
        => (TempDiffInfo[] tempDiffInfos) {
        // If the sequence is matched, the data is pushed/published to the output stream.
            tempDiffInfoStream.publish(tempDiffInfos);
        }
    }
}

function main(string... args) {

    index = 0;
    // Deploy the streaming sequence rules.
    deployPeakTempDetectionRules();

    // Subscribe to the `printInitialAndPeakTemp` function. This prints the peak temperature values.
    tempDiffInfoStream.subscribe(printInitalAndPeakTemp);

    // Simulating the data that is being sent to the `tempStream` stream.
    DeviceTempInfo t1 = { deviceID: 1, roomNo: 23, temp: 20.0 };
    DeviceTempInfo t2 = { deviceID: 1, roomNo: 23, temp: 22.5 };
    DeviceTempInfo t3 = { deviceID: 1, roomNo: 23, temp: 23.0 };
    DeviceTempInfo t4 = { deviceID: 1, roomNo: 23, temp: 21.0 };
    DeviceTempInfo t5 = { deviceID: 1, roomNo: 23, temp: 24.0 };
    DeviceTempInfo t6 = { deviceID: 1, roomNo: 23, temp: 23.9 };

    // Start simulating the events with the temperature readings.
    tempStream.publish(t1);
    runtime:sleep(200);

    tempStream.publish(t2);
    runtime:sleep(200);

    tempStream.publish(t3);
    runtime:sleep(200);

    tempStream.publish(t4);
    runtime:sleep(200);

    tempStream.publish(t5);
    runtime:sleep(200);

    tempStream.publish(t6);

    // Finished sending all six temperature events.

    // Wait until the results are collected.
    int count = 0;
    while (true) {
        runtime:sleep(500);
        count++;
        if ((lengthof tempDiffInfoArray) > 1 || count == 10) {
            break;
        }
    }
}

// The function that prints the peak temperature readings.
function printInitalAndPeakTemp(TempDiffInfo tempDiff) {
    io:println("printInitalAndPeakTemp function is invoked. " +
            "InitialTemp : " + tempDiff.initialTemp +
            " and Peak temp : " + tempDiff.peakTemp);
    addToGlobalTempDiffArray(tempDiff);
}

// The function that keeps track of all the temperature peak values.
function addToGlobalTempDiffArray(TempDiffInfo s) {
    tempDiffInfoArray[index] = s;
    index = index + 1;
}

