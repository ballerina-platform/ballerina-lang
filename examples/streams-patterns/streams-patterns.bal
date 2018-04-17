import ballerina/io;
import ballerina/runtime;

int index;

// type representing the regulator state
type RegulatorState {
    int deviceId;
    int roomNo;
    float tempSet;
    string userAction;
};

// type representig the user actions on the hotel key
type RoomKeyAction {
    int roomNo;
    string userAction;
};


RoomKeyAction[] roomActions = [];

stream<RegulatorState> regulatorStateChangeStream;
stream<RoomKeyAction> roomKeyStream;
stream<RoomKeyAction> regulatorActionStream;

// function deploy the decision rules for regulator's next action based on current regulator state and user action on
// hotel key
function deployRegulatorActionDecisionRules() {
    forever {
        from every regulatorStateChangeStream where userAction == "on" as e1
        followed by roomKeyStream where e1.roomNo == roomNo && userAction == "removed" as e2
        || regulatorStateChangeStream where e1.roomNo == roomNo && userAction == "off" as e3
        select e1.roomNo as roomNo, e2 == null ? "none" : "stop" as userAction having userAction != "none"
        => (RoomKeyAction[] keyAction) {
            regulatorActionStream.publish(keyAction);
        }
    }
}

function main(string... args) {
    index = 0;

    // Deploying the streaming pattern rules which define how the regulator is controlled based on received events
    deployRegulatorActionDecisionRules();

    // Sample events which represents the different regulator states
    RegulatorState regulatorState1 = {deviceId:1, roomNo:2, tempSet:23.56, userAction:"on"};
    RegulatorState regulatorState2 = {deviceId:1, roomNo:2, tempSet:23.56, userAction:"off"};

    // Sample event which represents the user action on the Door of the Room, 'removed' means the room is closed (in
    // other words the owner has left the room).
    RoomKeyAction roomKeyAction = {roomNo:2, userAction:"removed"};

    // RegulatorActionStream is subscribed to the 'alertRoomAction' function. Whenever the stream 'RegulatorActionStream'
    // receives a valid event, the function will be called
    regulatorActionStream.subscribe(alertRoomAction);

    // Publish/simulate the sample event which reprements the regulator 'switch on' event.
    regulatorStateChangeStream.publish(regulatorState1);
    runtime:sleepCurrentWorker(200);

    // Simulate the sample event which represents the door/room closed event
    roomKeyStream.publish(roomKeyAction);
    runtime:sleepCurrentWorker(500);

    int count = 0;
    while(true) {
        runtime:sleepCurrentWorker(500);
        count++;
        if((lengthof roomActions) > 0 || count == 10) {
            break;
        }
    }

}

function alertRoomAction(RoomKeyAction action) {
    io:println("alertRoomAction function invoked for Room:" + action.roomNo + " and the action :" +
            action.userAction);
    addToGlobalRoomActions(action);
}

function addToGlobalRoomActions(RoomKeyAction roomAction) {
    roomActions[index] = roomAction;
    index = index + 1;
}