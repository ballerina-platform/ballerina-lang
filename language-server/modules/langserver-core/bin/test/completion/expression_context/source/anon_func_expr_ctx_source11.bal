public type FT function(int, int, string, float...) returns R1;

type R1 record {
    int field1?;
    int field2;
};

public function testFunction() {
    FT x = function(int a, int b, string c, float... d) returns R1 {
        R1 rec = {field2: 0};
        return 
    };
}
