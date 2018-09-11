public type StreamEvent object {
    public EventType eventType;
    public int timestamp;
    public map data;

    public new((string, map) | map eventData, eventType, timestamp) {
        match eventData {
            (string, map) t => {
                foreach k, v in t[1] {
                    data[t[0] + DELIMITER + k] = v;
                }
            }
            map m => {
                data = m;
            }
        }
    }

    public function clone() returns StreamEvent {
        StreamEvent clone = new(cloneData(), eventType, timestamp);
        return clone;
    }

    public function addData(map eventData) {
        foreach k, v in eventData {
            data[k] = v;
        }
    }

    function cloneData() returns map {
        map dataClone;
        foreach k, v in data {
            dataClone[k] = v;
        }
        return dataClone;
    }
};