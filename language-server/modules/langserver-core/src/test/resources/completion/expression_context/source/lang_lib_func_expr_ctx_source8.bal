import ballerina/lang.'map;

function name() {
    map<int> mp = {
        "a": 1,
        "b": 2
    };
    _ = 'map:'map(mp, f);
}
