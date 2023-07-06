type PositionRec record {|
    int line;
    int offset;
|};

type Position readonly & PositionRec;

type Range record {|
    Position startLine;
    Position endLine;
|};

function test() {
    Range range = {
        
    };
}
