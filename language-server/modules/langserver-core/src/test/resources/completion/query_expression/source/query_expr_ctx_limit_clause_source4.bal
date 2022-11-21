import ballerina/module1;

const intVar = 0;

function findToptenCountriesByAthletes() returns error? {
    int zlimit = 10;
    string alimit = "";
    int xlimit = 2;
    map<int> athletesByCountry = check findNumOfAthletesBycountry();
    string[] listResult = from [string,int] entry in athletesByCountry.entries()
                     order by entry[1]
                     limit  select entry[0];

}

function findNumOfAthletesBycountry() returns map<int>|error {
    return {};
}
function funcInt() returns int {
    return 0;
}
function funcString() returns string {
    return "";
}
