public int four = 4;

public type SampleRecord record {
    int id;
    string name;
};

function run() {
    float val = <float> four;
    SampleRecord rec1 = {id: four, name: ""};
}

public type BTable table<map<int>>|BarTable;

public type BarTable table<Bar>key(a);

type Bar record {
    readonly string a;
};

public type ListenerRef Listener;

public class Listener {
    boolean initialized = false;

    public function init() {
        self.initialized = true;
    }
}
