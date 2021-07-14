
public type PositionRec record {|
    int line;
    int offset;
|};

public type Position readonly & PositionRec;

public type Range record {|
    Position startLine;
    Position endLine;
|};

public type Edit record {|
    Range range;
    string edit;
|};
