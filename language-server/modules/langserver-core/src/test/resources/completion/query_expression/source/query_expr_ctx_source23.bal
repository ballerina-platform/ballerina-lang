import ballerina/module1;

function testFunc() returns error? {
    map<int> athletesByCountry = {};
    stream<MedalStats, error?> medalStats;

    var result = from [string,int] entry in athletesByCountry.entries() 
    join var stats in medalStats on entry[0] equals stats.country 
    let float ratio = stats.totalMedals * s
}

type MedalStats record {
    string country;
    int totalMedals;
};
