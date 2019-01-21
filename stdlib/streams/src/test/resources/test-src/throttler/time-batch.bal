import ballerina/streams;
import ballerina/time;

public type TimeBatch object {
    public string EXPIRY_TIME_STAMP;
    public int timeInMilliSeconds = -1;
    public streams:LinkedList expiredEventQueue;
    public streams:Scheduler scheduler;
    public int expiredEventTime = -1;
    public int startTime = -1;
    public any[] windowParameters;
    public function (streams:StreamEvent[])? nextProcessPointer;

    public function __init(function(streams:StreamEvent[])? nextProcessPointer, any[] windowParameters) {
        self.scheduler = new(function (streams:StreamEvent[] events) {
                self.process(events);
            });
        self.expiredEventQueue = new;
        self.EXPIRY_TIME_STAMP = "expiryTimestamp";
        self.windowParameters = windowParameters;
        self.nextProcessPointer = nextProcessPointer;
        self.initParameters(windowParameters);
    }

    public function initParameters(any[] windowParameters) {
        if (windowParameters.length() == 1) {
            self.timeInMilliSeconds = <int> windowParameters[0];
        } else if (windowParameters.length() == 2) {
            self.timeInMilliSeconds = <int>windowParameters[0];
            self.startTime = <int> windowParameters[1];
        } else {
            error err = error("Can not have more than 2 parameters");
            panic err;
        }
    }

    public function process(streams:StreamEvent[] streamEvents) {
        lock {
            if (self.expiredEventTime == -1) {
                int currentTime = time:currentTime().time;
                if (self.startTime != -1) {
                    self.expiredEventTime = self.addTimeShift(currentTime);
                } else {
                    self.expiredEventTime = time:currentTime().time + self.timeInMilliSeconds;
                }
                self.scheduler.notifyAt(self.expiredEventTime);
            }

            int currentTime = time:currentTime().time;
            boolean sendEvents;
            if (currentTime >= self.expiredEventTime) {
                self.expiredEventTime += self.timeInMilliSeconds;
                self.scheduler.notifyAt(self.expiredEventTime);
                sendEvents = true;
            } else {
                sendEvents = false;
            }

            foreach var event in streamEvents {
                streams:StreamEvent streamEvent = <streams:StreamEvent>event;
                if (streamEvent.eventType != streams:CURRENT) {
                    continue;
                }
                streamEvent.addAttribute(self.EXPIRY_TIME_STAMP, self.expiredEventTime);
                streams:StreamEvent clonedEvent = streamEvent.copy();
                clonedEvent.eventType = streams:EXPIRED;
                self.expiredEventQueue.addLast(clonedEvent);
            }

            if (sendEvents) {
                self.expiredEventQueue.resetToFront();
                while (self.expiredEventQueue.hasNext()) {
                    streams:StreamEvent streamEvent = streams:getStreamEvent(self.expiredEventQueue.next());
                    streamEvents[streamEvents.length()] = streamEvent;
                }
                self.expiredEventQueue.clear();
            }
        }

        if (streamEvents.length() > 0) {
            self.nextProcessPointer.call(streamEvents);
        }
    }

    public function addTimeShift(int currentTime) returns int {
        int timePassedUntilNow = (currentTime - self.startTime) % self.timeInMilliSeconds;
        return currentTime + (self.timeInMilliSeconds - timePassedUntilNow);
    }

    public function getCandidateEvents(
                        streams:StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns (streams:StreamEvent?, streams:StreamEvent?)[] {
        // do nothing;
        return [((), ())];
    }
};

public function timeBatch(any[] windowParameters, function (streams:StreamEvent[])? nextProcessPointer = ())
                    returns streams:Window {
    TimeBatch timeBatchProcessor = new(nextProcessPointer, windowParameters);
    return timeBatchProcessor;
}