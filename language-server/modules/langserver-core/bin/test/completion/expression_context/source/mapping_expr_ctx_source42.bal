type Position readonly & record {|
    int line;
    int offset;
|};

type Range readonly & record {|
    Position startLine;
    Position endLine;
|};

function test() {
    Range range = {
        
    };
}
