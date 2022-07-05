function findTop10GoldMedalsToAthletesRatio() returns error? {
    map<int> athletesByCountry;
    stream<MedalStats, error?> medalStats;

    var result = from [string,int] entry in athletesByCountry.entries() join 
}

type MedalStats record {
    
};
