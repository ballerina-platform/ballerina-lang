type Input record {
    string stopId;
    Past[] past;
    Event event;
};

type Output record {
    record {
        string region;
        record {
            int impact;
            int completions;
            string period;
        }[] past;
    }[] location;
    record {
        string id;
        int batch;
    } event;
};

type Location record {
    int x;
    int y;
};

type Past record {
    int allJobs;
    int failures;
    string window;
};

type Event record {
    string id;
    int batch;
};

function generateReport(Input input) returns error? {
    Output output = {
        event: input.stopId == "" ? input.event : {id: "", batch: 0},
        location: from var location in getLocations()
            select {
                region: getRegion(location.x, location.y),
                past: from var entry in input.past
                    select {
                        impact: 1,
                        completions: entry.allJobs - entry.failures,
                        period: entry.window
                    }
            }
    };
}

function getLocations() returns Location[] {
    return [
        {
            x: 0,
            y: 0
        }
    ];
}

function getRegion(int x, int y) returns string {
    return "";
}
