
public const map<map<boolean>> bm3 = { "key3": bm1, "key4": bm2 };
public const map<boolean> bm1 = { "key1": true };
public const map<boolean> bm2 = { "key2": false };

public const map<map<boolean>> bm3_new = { "key3": bm1_new, "key4": bm2_new };
public const map<boolean> bm1_new = { "key1": true };
public const map<boolean> bm2_new = { "key2": false };

public const map<map<boolean>> bm7 = { "key3": { "key1": true }, "key2": { "key4": false } };
public const map<map<boolean>> bm8 = { "key3": { "key1": true }, "key2": { "key4": false } };

// -----------------------------------------------------------

public const map<map<int>> im3 = { "key3": im1, "key4": im2 };
public const map<int> im1 = { "key1": 1 };
public const map<int> im2 = { "key2": 2 };

public const map<map<int>> im3_new = { "key3": im1_new, "key4": im2_new };
public const map<int> im1_new = { "key1": 1 };
public const map<int> im2_new = { "key2": 2 };

public const map<map<int>> im7 = { "key3": { "key1": 1 }, "key4": { "key2": 2 } };
public const map<map<int>> im8 = { "key3": { "key1": 1 }, "key4": { "key2": 2 } };

// -----------------------------------------------------------

public const map<map<byte>> bytem3 = { "key3": bytem1, "key4": bytem2 };
public const map<byte> bytem1 = { "key1": 10 };
public const map<byte> bytem2 = { "key2": 20 };

public const map<map<byte>> bytem3_new = { "key3": bytem1_new, "key4": bytem2_new };
public const map<byte> bytem1_new = { "key1": 10 };
public const map<byte> bytem2_new = { "key2": 20 };

public const map<map<byte>> bytem7 = { "key3": { "key1": 10 }, "key4": { "key2": 20 } };
public const map<map<byte>> bytem8 = { "key3": { "key1": 10 }, "key4": { "key2": 20 } };

// -----------------------------------------------------------

public const map<map<float>> fm3 = { "key3": fm1, "key4": fm2 };
public const map<float> fm1 = { "key1": 2.0 };
public const map<float> fm2 = { "key2": 4.0 };

public const map<map<float>> fm3_new = { "key3": fm1_new, "key4": fm2_new };
public const map<float> fm1_new = { "key1": 2.0 };
public const map<float> fm2_new = { "key2": 4.0 };

public const map<map<float>> fm7 = { "key3": { "key1": 2.0 }, "key4": { "key2": 4.0 } };
public const map<map<float>> fm8 = { "key3": { "key1": 2.0 }, "key4": { "key2": 4.0 } };

// -----------------------------------------------------------

public const map<map<decimal>> dm3 = { "key3": dm1, "key4": dm2 };
public const map<decimal> dm1 = { "key1": 100 };
public const map<decimal> dm2 = { "key2": 200 };

public const map<map<decimal>> dm3_new = { "key3": dm1_new, "key4": dm2_new };
public const map<decimal> dm1_new = { "key1": 100 };
public const map<decimal> dm2_new = { "key2": 200 };

public const map<map<decimal>> dm7 = { "key3": { "key1": 100 }, "key4": { "key2": 200 } };
public const map<map<decimal>> dm8 = { "key3": { "key1": 100 }, "key4": { "key2": 200 } };

// -----------------------------------------------------------

public const map<map<string>> sm3 = { "key3": sm1, "key4": sm2 };
public const map<string> sm1 = { "key1": "value1" };
public const map<string> sm2 = { "key2": "value2" };

public const map<map<string>> sm3_new = { "key3": sm1_new, "key4": sm2_new };
public const map<string> sm1_new = { "key1": "value1" };
public const map<string> sm2_new = { "key2": "value2" };

public const map<map<string>> sm7 = { "key3": { "key1": "value1" }, "key4": { "key2": "value2" } };
public const map<map<string>> sm8 = { "key3": { "key1": "value1" }, "key4": { "key2": "value2" } };

// -----------------------------------------------------------

public const map<map<()>> nm3 = { "key3": nm1, "key4": nm2 };
public const map<()> nm1 = { "key1": () };
public const map<()> nm2 = { "key2": () };

public const map<map<()>> nm3_new = { "key3": nm1_new, "key4": nm2_new };
public const map<()> nm1_new = { "key1": () };
public const map<()> nm2_new = { "key2": () };

public const map<map<()>> nm7 = { "key3": { "key1": () }, "key4": { "key2": () } };
public const map<map<()>> nm8 = { "key3": { "key1": () }, "key4": { "key2": () } };

// -----------------------------------------------------------

public const map<map<map<string>>> m3 = { "k3": m2 };

const map<map<string>> m2 = { "k2": m1 };

const map<string> m1 = { "k1": sVal };

const sVal = "v1";

public const sConst = "Ballerina";
public const iConst = 100;
public const map<string> mConst = { "mKey": "mValue" };

public type TestConfig record {|
    string s;
    int i;
    map<string> m;
|};

public annotation<function> testAnnotation TestConfig;

map<map<string>> m5 = { "m5k": m6 };
public const map<string> m6 = { "m6k": "m6v" };

function getM5() returns map<map<string>> {
    return m5;
}

map<string>[] a1 = [m6];

function getA1() returns map<string>[] {
    return a1;
}

public const map<boolean> bm4 = { "bm4k": true };
public const map<boolean> bm5 = { "bm5kn": bm4.bm4k };

public const map<int> im4 = { "im4k": 123 };
public const map<int> im5 = { "im5kn": im4.im4k };

public const map<byte> bytem4 = { "bytem4k": 64 };
public const map<byte> bytem5 = { "bytem5kn": bytem4.bytem4k };

public const map<float> fm4 = { "fm4k": 12.5 };
public const map<float> fm5 = { "fm5kn": fm4.fm4k };

public const map<decimal> dm4 = { "dm4k": 5.56 };
public const map<decimal> dm5 = { "dm5kn": dm4.dm4k };

public const map<string> sm4 = { "sm4k": "sm4v" };
public const map<string> sm5 = { "sm5kn": sm4.sm4k };

public const map<()> nm4 = { "nm4k": () };
public const map<()> nm5 = { "nm5kn": nm4.nm4k };

// -----------------------------------------------------------

map<map<boolean>> bm10 = { "bm10k": bm11 };
public const map<boolean> bm11 = { "bm11k": true };

public function getBM10() returns map<map<boolean>> {
    return bm10;
}

map<boolean>[] ba1 = [bm11];

public function getBA1() returns map<boolean>[] {
    return ba1;
}

map<map<int>> im10 = { "im10k": im11 };
public const map<int> im11 = { "im11k": 10 };

public function getIM10() returns map<map<int>> {
    return im10;
}

map<int>[] ia1 = [im11];

public function getIA1() returns map<int>[] {
    return ia1;
}

map<map<byte>> bytem10 = { "bytem10k": bytem11 };
public const map<byte> bytem11 = { "bytem11k": 4 };

public function getBYTEM10() returns map<map<byte>> {
    return bytem10;
}

map<byte>[] bytea1 = [bytem11];

public function getBYTEA1() returns map<byte>[] {
    return bytea1;
}

map<map<float>> fm10 = { "fm10k": fm11 };
public const map<float> fm11 = { "fm11k": 40.0 };

public function getFM10() returns map<map<float>> {
    return fm10;
}

map<float>[] fa1 = [fm11];

public function getFA1() returns map<float>[] {
    return fa1;
}

map<map<decimal>> dm10 = { "dm10k": dm11 };
public const map<decimal> dm11 = { "dm11k": 125 };

public function getDM10() returns map<map<decimal>> {
    return dm10;
}

map<decimal>[] da1 = [dm11];

public function getDA1() returns map<decimal>[] {
    return da1;
}

map<map<string>> sm10 = { "sm10k": sm11 };
public const map<string> sm11 = { "sm11k": "sm11v" };

public function getSM10() returns map<map<string>> {
    return sm10;
}

map<string>[] sa1 = [sm11];

public function getSA1() returns map<string>[] {
    return sa1;
}

map<map<()>> nm10 = { "nm10k": nm11 };
public const map<()> nm11 = { "nm11k": () };

public function getNM10() returns map<map<()>> {
    return nm10;
}

map<()>[] na1 = [nm11];

public function getNA1() returns map<()>[] {
    return na1;
}
