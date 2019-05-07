// boolean ---------------------------------------------------

const map<boolean> bm1 = {};
const map<boolean> bm2 = { "key": bm1.key };

const map<map<boolean>> bm3 = {};
const map<boolean> bm4 = { "key": bm3.key.key };

const map<boolean> bm5 = {};
map<boolean> bm6 = { "key": bm5.key};

const map<map<boolean>> bm7 = {};
map<boolean> bm8 = { "key": bm7.key.key};

map<boolean> bm10 = {};
const map<boolean> bm11 = { "key": bm10.key};

map<map<boolean>> bm12 = {};
const map<boolean> bm13 = { "key": bm12.key.key};

const map<boolean> bm15 = { getKey(): getBooleanValue() };

function getBooleanValue() returns boolean {
    return true;
}

// int -------------------------------------------------------

const map<int> im1 = {};
const map<int> im2 = { "key": im1.key };

const map<map<int>> im3 = {};
const map<int> im4 = { "key": im3.key.key };

const map<int> im5 = {};
map<int> im6 = { "key": im5.key};

const map<map<int>> im7 = {};
map<int> im8 = { "key": im7.key.key};

map<int> im10 = {};
const map<int> im11 = { "key": im10.key};

map<map<int>> im12 = {};
const map<int> im13 = { "key": im12.key.key};

const map<int> im15 = { getKey(): getIntValue() };

function getIntValue() returns int {
    return 10;
}

// byte ------------------------------------------------------

const map<byte> bytem1 = {};
const map<byte> bytem2 = { "key": bytem1.key };

const map<map<byte>> bytem3 = {};
const map<byte> bytem4 = { "key": bytem3.key.key };

const map<byte> bytem5 = {};
map<byte> bytem6 = { "key": bytem5.key};

const map<map<byte>> bytem7 = {};
map<byte> bytem8 = { "key": bytem7.key.key};

map<byte> bytem10 = {};
const map<byte> bytem11 = { "key": bytem10.key};

map<map<byte>> bytem12 = {};
const map<byte> bytem13 = { "key": bytem12.key.key};

const map<byte> bytem15 = { getKey(): getByteValue() };

function getByteValue() returns byte {
    return 64;
}

// float -----------------------------------------------------

const map<float> fm1 = {};
const map<float> fm2 = { "key": fm1.key };

const map<map<float>> fm3 = {};
const map<float> fm4 = { "key": fm3.key.key };

const map<float> fm5 = {};
map<float> fm6 = { "key": fm5.key};

const map<map<float>> fm7 = {};
map<float> fm8 = { "key": fm7.key.key};

map<float> fm10 = {};
const map<float> fm11 = { "key": fm10.key};

map<map<float>> fm12 = {};
const map<float> fm13 = { "key": fm12.key.key};

const map<float> fm15 = { getKey(): getFloatValue() };

function getFloatValue() returns float {
    return 12.5;
}

// decimal ---------------------------------------------------

const map<decimal> dm1 = {};
const map<decimal> dm2 = { "key": dm1.key };

const map<map<decimal>> dm3 = {};
const map<decimal> dm4 = { "key": dm3.key.key };

const map<decimal> dm5 = {};
map<decimal> dm6 = { "key": dm5.key};

const map<map<decimal>> dm7 = {};
map<decimal> dm8 = { "key": dm7.key.key};

map<decimal> dm10 = {};
const map<decimal> dm11 = { "key": dm10.key};

map<map<decimal>> dm12 = {};
const map<decimal> dm13 = { "key": dm12.key.key};

const map<decimal> dm15 = { getKey(): getDecimalValue() };

function getDecimalValue() returns decimal {
    return 25;
}

// string ----------------------------------------------------

const map<string> sm1 = {};
const map<string> sm2 = { "key": sm1.key };

const map<map<string>> sm3 = {};
const map<string> sm4 = { "key": sm3.key.key };

const map<string> sm5 = {};
map<string> sm6 = { "key": sm5.key};

const map<map<string>> sm7 = {};
map<string> sm8 = { "key": sm7.key.key};

map<string> sm10 = {};
const map<string> sm11 = { "key": sm10.key};

map<map<string>> sm12 = {};
const map<string> sm13 = { "key": sm12.key.key};

const map<string> sm15 = { getKey(): getStringValue() };

function getStringValue() returns string {
    return "Shan";
}

// nil -------------------------------------------------------

const map<()> nm1 = {};
const map<()> nm2 = { "key": nm1.key };

const map<map<()>> nm3 = {};
const map<()> nm4 = { "key": nm3.key.key };

const map<()> nm5 = {};
map<()> nm6 = { "key": nm5.key};

const map<map<()>> nm7 = {};
map<()> nm8 = { "key": nm7.key.key};

map<()> nm10 = {};
const map<()> nm11 = { "key": nm10.key};

map<map<()>> nm12 = {};
const map<()> nm13 = { "key": nm12.key.key};

const map<()> nm15 = { getKey(): getNilValue() };

function getNilValue() returns () {
    return ();
}

// -----------------------------------------------------------

function getKey() returns string {
    return "key";
}
