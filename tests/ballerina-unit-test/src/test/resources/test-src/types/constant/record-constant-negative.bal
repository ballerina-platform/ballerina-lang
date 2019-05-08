// boolean ---------------------------------------------------

const record {| boolean...; |} br1 = {};
const record {| boolean...; |} br2 = { key: br1.key };

const record {| record {| boolean...; |}...; |} br3 = {};
const record {| boolean...; |} br4 = { key: br3.key.key };

const record {| boolean...; |} br5 = {};
record {| boolean...; |} br6 = { key: br5.key};

const record {| record {| boolean...; |}...; |} br7 = {};
record {| boolean...; |} br8 = { key: br7.key.key};

record {| boolean...; |} br10 = {};
const record {| boolean...; |} br11 = { key: br10.key};

record {| record {| boolean...; |}...; |} br12 = {};
const record {| boolean...; |} br13 = { key: br12.key.key};

const record {| boolean...; |} br15 = { getKey(): getBooleanValue() };

function getBooleanValue() returns boolean {
    return true;
}

// int -------------------------------------------------------

const record {| int...; |} ir1 = {};
const record {| int...; |} ir2 = { key: ir1.key };

const record {| record {| int...; |}...; |} ir3 = {};
const record {| int...; |} ir4 = { key: ir3.key.key };

const record {| int...; |} ir5 = {};
record {| int...; |} ir6 = { key: ir5.key};

const record {| record {| int...; |}...; |} ir7 = {};
record {| int...; |} ir8 = { key: ir7.key.key};

record {| int...; |} ir10 = {};
const record {| int...; |} ir11 = { key: ir10.key};

record {| record {| int...; |}...; |} ir12 = {};
const record {| int...; |} ir13 = { key: ir12.key.key};

const record {| int...; |} ir15 = { getKey(): getIntValue() };

function getIntValue() returns int {
    return 10;
}

// byte ------------------------------------------------------

const record {| byte...; |} byter1 = {};
const record {| byte...; |} byter2 = { key: byter1.key };

const record {| record {| byte...; |}...; |} byter3 = {};
const record {| byte...; |} byter4 = { key: byter3.key.key };

const record {| byte...; |} byter5 = {};
record {| byte...; |} byter6 = { key: byter5.key};

const record {| record {| byte...; |}...; |} byter7 = {};
record {| byte...; |} byter8 = { key: byter7.key.key};

record {| byte...; |} byter10 = {};
const record {| byte...; |} byter11 = { key: byter10.key};

record {| record {| byte...; |}...; |} byter12 = {};
const record {| byte...; |} byter13 = { key: byter12.key.key};

const record {| byte...; |} byter15 = { getKey(): getByteValue() };

function getByteValue() returns byte {
    return 64;
}

// float -----------------------------------------------------

const record {| float...; |} fr1 = {};
const record {| float...; |} fr2 = { key: fr1.key };

const record {| record {| float...; |}...; |} fr3 = {};
const record {| float...; |} fr4 = { key: fr3.key.key };

const record {| float...; |} fr5 = {};
record {| float...; |} fr6 = { key: fr5.key};

const record {| record {| float...; |}...; |} fr7 = {};
record {| float...; |} fr8 = { key: fr7.key.key};

record {| float...; |} fr10 = {};
const record {| float...; |} fr11 = { key: fr10.key};

record {| record {| float...; |}...; |} fr12 = {};
const record {| float...; |} fr13 = { key: fr12.key.key};

const record {| float...; |} fr15 = { getKey(): getFloatValue() };

function getFloatValue() returns float {
    return 12.5;
}

// decimal ---------------------------------------------------

const record {| decimal...; |} dr1 = {};
const record {| decimal...; |} dr2 = { key: dr1.key };

const record {| record {| decimal...; |}...; |} dr3 = {};
const record {| decimal...; |} dr4 = { key: dr3.key.key };

const record {| decimal...; |} dr5 = {};
record {| decimal...; |} dr6 = { key: dr5.key};

const record {| record {| decimal...; |}...; |} dr7 = {};
record {| decimal...; |} dr8 = { key: dr7.key.key};

record {| decimal...; |} dr10 = {};
const record {| decimal...; |} dr11 = { key: dr10.key};

record {| record {| decimal...; |}...; |} dr12 = {};
const record {| decimal...; |} dr13 = { key: dr12.key.key};

const record {| decimal...; |} dr15 = { getKey(): getDecimalValue() };

function getDecimalValue() returns decimal {
    return 25;
}

// string ----------------------------------------------------

const record {| string...; |} sr1 = {};
const record {| string...; |} sr2 = { key: sr1.key };

const record {| record {| string...; |}...; |} sr3 = {};
const record {| string...; |} sr4 = { key: sr3.key.key };

const record {| string...; |} sr5 = {};
record {| string...; |} sr6 = { key: sr5.key};

const record {| record {| string...; |}...; |} sr7 = {};
record {| string...; |} sr8 = { key: sr7.key.key};

record {| string...; |} sr10 = {};
const record {| string...; |} sr11 = { key: sr10.key};

record {| record {| string...; |}...; |} sr12 = {};
const record {| string...; |} sr13 = { key: sr12.key.key};

const record {| string...; |} sr15 = { getKey(): getStringValue() };

function getStringValue() returns string {
    return "Shan";
}

// nil -------------------------------------------------------

const record {| ()...; |} nr1 = {};
const record {| ()...; |} nr2 = { key: nr1.key };

const record {| record {| ()...; |}...; |} nr3 = {};
const record {| ()...; |} nr4 = { key: nr3.key.key };

const record {| ()...; |} nr5 = {};
record {| ()...; |} nr6 = { key: nr5.key};

const record {| record {| ()...; |}...; |} nr7 = {};
record {| ()...; |} nr8 = { key: nr7.key.key};

record {| ()...; |} nr10 = {};
const record {| ()...; |} nr11 = { key: nr10.key};

record {| record {| ()...; |}...; |} nr12 = {};
const record {| ()...; |} nr13 = { key: nr12.key.key};

const record {| ()...; |} nr15 = { getKey(): getNilValue() };

function getNilValue() returns () {
    return ();
}

// -----------------------------------------------------------

function getKey() returns string {
    return "key";
}

// -----------------------------------------------------------

type R1 record {|
    any d1;
    string d2;
    any...;
|};

const int i1 = 10;

const R1 r1 = { d1: "Hello", d2: "Ballerina", x: i1 };

// -----------------------------------------------------------

type R2 record {
    any d1;
    string d2;
};

const int i2 = 10;

const R2 r2 = { d1: "Hello", d2: "Ballerina", x: i2 };
