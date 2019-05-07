// boolean ---------------------------------------------------

const record {| boolean...; |} bm1 = {};
const record {| boolean...; |} bm2 = { key: bm1.key };

const record {| record {| boolean...; |}...; |} bm3 = {};
const record {| boolean...; |} bm4 = { key: bm3.key.key };

const record {| boolean...; |} bm5 = {};
record {| boolean...; |} bm6 = { key: bm5.key};

const record {| record {| boolean...; |}...; |} bm7 = {};
record {| boolean...; |} bm8 = { key: bm7.key.key};

record {| boolean...; |} bm10 = {};
const record {| boolean...; |} bm11 = { key: bm10.key};

record {| record {| boolean...; |}...; |} bm12 = {};
const record {| boolean...; |} bm13 = { key: bm12.key.key};

const record {| boolean...; |} bm15 = { getKey(): getBooleanValue() };

function getBooleanValue() returns boolean {
    return true;
}

// int -------------------------------------------------------

const record {| int...; |} im1 = {};
const record {| int...; |} im2 = { key: im1.key };

const record {| record {| int...; |}...; |} im3 = {};
const record {| int...; |} im4 = { key: im3.key.key };

const record {| int...; |} im5 = {};
record {| int...; |} im6 = { key: im5.key};

const record {| record {| int...; |}...; |} im7 = {};
record {| int...; |} im8 = { key: im7.key.key};

record {| int...; |} im10 = {};
const record {| int...; |} im11 = { key: im10.key};

record {| record {| int...; |}...; |} im12 = {};
const record {| int...; |} im13 = { key: im12.key.key};

const record {| int...; |} im15 = { getKey(): getIntValue() };

function getIntValue() returns int {
    return 10;
}

// byte ------------------------------------------------------

const record {| byte...; |} bytem1 = {};
const record {| byte...; |} bytem2 = { key: bytem1.key };

const record {| record {| byte...; |}...; |} bytem3 = {};
const record {| byte...; |} bytem4 = { key: bytem3.key.key };

const record {| byte...; |} bytem5 = {};
record {| byte...; |} bytem6 = { key: bytem5.key};

const record {| record {| byte...; |}...; |} bytem7 = {};
record {| byte...; |} bytem8 = { key: bytem7.key.key};

record {| byte...; |} bytem10 = {};
const record {| byte...; |} bytem11 = { key: bytem10.key};

record {| record {| byte...; |}...; |} bytem12 = {};
const record {| byte...; |} bytem13 = { key: bytem12.key.key};

const record {| byte...; |} bytem15 = { getKey(): getByteValue() };

function getByteValue() returns byte {
    return 64;
}

// float -----------------------------------------------------

const record {| float...; |} fm1 = {};
const record {| float...; |} fm2 = { key: fm1.key };

const record {| record {| float...; |}...; |} fm3 = {};
const record {| float...; |} fm4 = { key: fm3.key.key };

const record {| float...; |} fm5 = {};
record {| float...; |} fm6 = { key: fm5.key};

const record {| record {| float...; |}...; |} fm7 = {};
record {| float...; |} fm8 = { key: fm7.key.key};

record {| float...; |} fm10 = {};
const record {| float...; |} fm11 = { key: fm10.key};

record {| record {| float...; |}...; |} fm12 = {};
const record {| float...; |} fm13 = { key: fm12.key.key};

const record {| float...; |} fm15 = { getKey(): getFloatValue() };

function getFloatValue() returns float {
    return 12.5;
}

// decimal ---------------------------------------------------

const record {| decimal...; |} dm1 = {};
const record {| decimal...; |} dm2 = { key: dm1.key };

const record {| record {| decimal...; |}...; |} dm3 = {};
const record {| decimal...; |} dm4 = { key: dm3.key.key };

const record {| decimal...; |} dm5 = {};
record {| decimal...; |} dm6 = { key: dm5.key};

const record {| record {| decimal...; |}...; |} dm7 = {};
record {| decimal...; |} dm8 = { key: dm7.key.key};

record {| decimal...; |} dm10 = {};
const record {| decimal...; |} dm11 = { key: dm10.key};

record {| record {| decimal...; |}...; |} dm12 = {};
const record {| decimal...; |} dm13 = { key: dm12.key.key};

const record {| decimal...; |} dm15 = { getKey(): getDecimalValue() };

function getDecimalValue() returns decimal {
    return 25;
}

// string ----------------------------------------------------

const record {| string...; |} sm1 = {};
const record {| string...; |} sm2 = { key: sm1.key };

const record {| record {| string...; |}...; |} sm3 = {};
const record {| string...; |} sm4 = { key: sm3.key.key };

const record {| string...; |} sm5 = {};
record {| string...; |} sm6 = { key: sm5.key};

const record {| record {| string...; |}...; |} sm7 = {};
record {| string...; |} sm8 = { key: sm7.key.key};

record {| string...; |} sm10 = {};
const record {| string...; |} sm11 = { key: sm10.key};

record {| record {| string...; |}...; |} sm12 = {};
const record {| string...; |} sm13 = { key: sm12.key.key};

const record {| string...; |} sm15 = { getKey(): getStringValue() };

function getStringValue() returns string {
    return "Shan";
}

// nil -------------------------------------------------------

const record {| ()...; |} nm1 = {};
const record {| ()...; |} nm2 = { key: nm1.key };

const record {| record {| ()...; |}...; |} nm3 = {};
const record {| ()...; |} nm4 = { key: nm3.key.key };

const record {| ()...; |} nm5 = {};
record {| ()...; |} nm6 = { key: nm5.key};

const record {| record {| ()...; |}...; |} nm7 = {};
record {| ()...; |} nm8 = { key: nm7.key.key};

record {| ()...; |} nm10 = {};
const record {| ()...; |} nm11 = { key: nm10.key};

record {| record {| ()...; |}...; |} nm12 = {};
const record {| ()...; |} nm13 = { key: nm12.key.key};

const record {| ()...; |} nm15 = { getKey(): getNilValue() };

function getNilValue() returns () {
    return ();
}

// -----------------------------------------------------------

function getKey() returns string {
    return "key";
}

// -----------------------------------------------------------

type R record {|
    any d1;
    string d2;
    any...;
|};

const int i = 10;

const R r = { d2: x: i };
