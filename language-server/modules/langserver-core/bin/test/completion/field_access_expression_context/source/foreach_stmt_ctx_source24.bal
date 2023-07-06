function foo(PathSegment[] segments, map<anydata> pathParams) {
    foreach var item in segments {
        if item is PathParam && pathParams.hasKey(item.) {
            
        }
    }
}

public type PathSegment string|PathParam;

type PathParam record {|
    string name;
|};
