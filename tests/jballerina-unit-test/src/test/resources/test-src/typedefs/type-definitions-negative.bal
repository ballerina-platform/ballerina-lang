type G object {
    function __init() {

    }
};

type T1 object { G g; };

type T2 int[]|map<string>|int|object { G g; }|error;

type T3 [object { G g; }, error];
