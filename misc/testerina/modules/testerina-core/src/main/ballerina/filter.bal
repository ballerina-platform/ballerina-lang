
string[] filterGroups = [];
string[] filterDisableGroups = [];
string[] filterTests = [];

public function setTestOptions(string groups, string disableGroups, string tests) {
    filterGroups = parseStringArrayInput(groups);
    filterDisableGroups = parseStringArrayInput(disableGroups);
    filterTests = parseStringArrayInput(tests);
}

function parseStringArrayInput(string arrArg) returns string[] => arrArg == "" ? [] : split(arrArg, ",");
