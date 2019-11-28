function testForEach() {
    foreach var item in getIntList() {
        var testVal = item;
    }

    foreach Country item in getCountryList() {
        Country country = item;
    }

    foreach [int, string, [int, Country], string...] [iVal, sVal, [iVal2, cVal], ...rst] in getTuples() {
        int iVal1 = iVal;
        Country country = cVal;
        string[] rest = rst;
    }

    foreach Country {name: coName, capital: {name, code: capCode}, ...restFields} in getMappings() {
        string countryName = coName;
        string capName = name;
        string capitalCode = capCode;
        map<anydata> rest = restFields;
    }
}

function getTuples() returns [int, string, [int, Country], string...] [] {
    [int, string, [int, Country], string...] [] arr = [];

    return arr;
}

function getMappings() returns Country[] {
    Country[] countries = [];

    return countries;
}

function getIntList() returns int[] {
    int[] arr = [];
    return arr;
}

function getCountryList() returns Country[] {
    Country[] cList = [];
    return cList;
}

type Country record {|
    string name;
    Capital capital;
    int...;
|};

type Capital record {
    string name;
    string code;
};
