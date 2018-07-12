import ballerina/io;
import ballerina/runtime;

int index;

// Create a record type that represents the regulator state.
type RegulatorState record {
    int deviceId;
    int roomNo;
    float tempSet;
    string userAction;
};

// Create a record type that represents the user actions on the hotel key.
type RoomKeyAction record {
    int roomNo;
    string userAction;
};


RoomKeyAction[] roomActions = [];

stream<RegulatorState> regulatorStateChangeStream;
stream<RoomKeyAction> roomKeyStream;
stream<RoomKeyAction> regulatorActionStream;

// Deploy the decision rules for the regulator's next action based on the current regulator state and the user action on
// the hotel key. If the regulator was on before and is still on after the user has removed the hotel key from the
// room, the `stop` control action is called.
function deployRegulatorActionDecisionRules() {
    forever {
        from every regulatorStateChangeStream
            where userAction == "on" as e1
        followed by roomKeyStream
            where e1.roomNo == roomNo && userAction == "removed" as e2
        || regulatorStateChangeStream
            where e1.roomNo == roomNo && userAction == "off" as e3
        select e1.roomNo as roomNo,
            e2 == null ? "none" : "stop" as userAction
        having userAction != "none"
        => (RoomKeyAction[] keyAction) {
            regulatorActionStream.publish(keyAction);
        }
    }
}

function main(string... args) {
    index = 0;

    // Deploys the streaming pattern rules that define how the regulator is controlled based on received events.
    deployRegulatorActionDecisionRules();

    // Sample events that represent the different regulator states.
    RegulatorState regulatorState1 = { deviceId: 1, roomNo: 2,
        tempSet: 23.56, userAction: "on" };
    RegulatorState regulatorState2 = { deviceId: 1, roomNo: 2,
        tempSet: 23.56, userAction: "off" };

    // A sample event that represents the user action on the door of the room. 'removed' indicates that the owner has left the room.
    RoomKeyAction roomKeyAction = { roomNo: 2,
                                        userAction: "removed" };

    // The `RegulatorActionStream` subscribes to the `alertRoomAction` function. Whenever the
    // 'RegulatorActionStream' stream receives a valid event, this function is called.
    regulatorActionStream.subscribe(alertRoomAction);

    // Publish/simulate the sample event that represents the regulator `switch on` event.
    regulatorStateChangeStream.publish(regulatorState1);
    runtime:sleep(200);

    // Simulate the sample event that represents the door/room closed event.
    roomKeyStream.publish(roomKeyAction);
    runtime:sleep(500);

    int count = 0;
    while (true) {
        runtime:sleep(500);
        count++;
        if ((lengthof roomActions) > 0 || count == 10) {
            break;
        }
    }

}

function alertRoomAction(RoomKeyAction action) {
    io:println("alertRoomAction function invoked for Room : " +
            action.roomNo + " and the action : " +
            action.userAction);
    addToGlobalRoomActions(action);
}

function addToGlobalRoomActions(RoomKeyAction roomAction) {
    roomActions[index] = roomAction;
    index = index + 1;
}
