function findTop10GoldMedalsToAthletesRatio() returns error? {
    map<int> athletesByCountry;
    stream<MedalStats, error?> medalStats;

    var ratios = from [string,int] entry in athletesByCountry.entries() 
    join var stats in medalStats on entry[0] equals stats.country 
    let float ratio = (<float>stats.gold/<float>entry[1]) * 100 order by ratio descending 
    limit 10
    select  {
        country: entry[0],
        ratio
    } 

}

type MedalStats record {
    int gold;
    string country;
};
