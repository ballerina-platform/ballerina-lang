import ballerina/io;

public type EventType "CURRENT"|"EXPIRED"|"ALL"|"RESET";

public type StreamEvent {
    EventType eventType;
    any eventObject;
    int timestamp;
};

public type LengthWindow object {
    public {
        int counter;
        int size;
        any[] events = [];
        stream outputStream;
        EventType eventType = "ALL";
    }

    new(int s, EventType evType) {
        size = s;
        eventType = evType;
    }

    public function add(any event) {
        events[counter % size] = event;
        counter = counter + 1;
        io:println(event);
    }

    public function returnContent() returns (any[]) {
        return events;
    }

    public function getEventToBeExpired() returns (any) {
        return events[counter % size];
    }
};

public function lengthWindow(int length, EventType  eventType) returns LengthWindow {
    LengthWindow lengthWindowObject = new(length, eventType);
    return lengthWindowObject;
}


type QNode object {
    public {
        any data;
        QNode? nextNode;
    }

    new(any data1) {
        self.data = data1;
    }

};

type Queue object {
    private {
        QNode? front;
        QNode? rear;
        QNode? iteratorFromRear;
    }

    public function isEmpty() returns boolean {
        match front {
            QNode value => {
                return false;
            }
            () => {
                return true;
            }
        }
    }

    public function peekRear() returns any {
        match rear {
            QNode value => {
                return value.data;
            }
            () => {
                return ();
            }
        }
    }

    public function peekFront() returns any {
        match front {
            QNode value => {
                return value.data;
            }
            () => {
                return ();
            }
        }
    }

    public function enqueue(any data1) {
        QNode temp = new(data1);
        match rear {
            QNode value => {
                value.nextNode = temp;
                rear = temp;
            }
            () => {
                rear = temp;
                front = rear;
            }
        }
    }

    public function dequeue() returns any? {
        match front {
            QNode value => {
                front = value.nextNode;
                match front {
                    QNode nextValue => {
                        // do nothing
                    }
                    () => {
                        rear = ();
                    }
                }
                return value.data;
            }
            () => {
                return ();
            }
        }
    }

    public function asArray() returns any[] {
        any[] anyArray = [];
        QNode? temp;
        int i;
        if (!isEmpty()) {
            match front {
                QNode value => {
                    temp = value;
                    while (temp != ()) {
                        anyArray[i] = temp.data;
                        i = i + 1;
                        temp = temp.nextNode;
                    }
                }
                () => {

                }
            }
        }
        return anyArray;
    }
};
