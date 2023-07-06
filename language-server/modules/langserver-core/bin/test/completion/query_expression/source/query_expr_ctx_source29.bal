function getIntArr() returns int[] { 
    return [];
}

function getStrArr() returns string[] { 
    return [];
}

type MedalStatsRecord record {

}; 

function getMedalStats() returns stream<MedalStatsRecord, error?> | error {
    int intValue;
    string stringValue; 
    int[] intArray;

    stream<string[], error?> tsvStream;

    return stream from var entry in 
}
