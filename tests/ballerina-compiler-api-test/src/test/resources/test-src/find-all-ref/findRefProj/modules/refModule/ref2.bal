public int four = 4;

public type SampleRecord record {
    int id;
    string name;
};

function run() {
    float val = <float> four;
    SampleRecord rec1 = {id: four, name: ""};
}
