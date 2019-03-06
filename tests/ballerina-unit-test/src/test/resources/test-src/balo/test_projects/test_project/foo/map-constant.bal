
public const map<map<boolean>> bm3 = { "key3": bm1, "key4": bm2 };
public const map<boolean> bm1 = { "key1": true };
public const map<boolean> bm2 = { "key2": false };

public const map<map<boolean>> bm3_new = { "key3": bm1_new, "key4": bm2_new };
public const map<boolean> bm1_new = { "key1": true };
public const map<boolean> bm2_new = { "key2": false };

public const map<map<boolean>> bm7 = { "key1": { "key3": true }, "key2": { "key4": false } };
public const map<map<boolean>> bm8 = { "key1": { "key3": true }, "key2": { "key4": false } };

// -----------------------------------------------------------

public const map<map<int>> im3 = { "key3": im1, "key4": im2 };
public const map<int> im1 = { "key1": 1 };
public const map<int> im2 = { "key2": 2 };

public const map<map<int>> im3_new = { "key3": im1_new, "key4": im2_new };
public const map<int> im1_new = { "key1": 1 };
public const map<int> im2_new = { "key2": 2 };

public const map<map<int>> im7 = { "key1": { "key3": 1 }, "key2": { "key4": 2 } };
public const map<map<int>> im8 = { "key1": { "key3": 1 }, "key2": { "key4": 2 } };

// -----------------------------------------------------------

public const map<map<byte>> bytem3 = { "key3": bytem1, "key4": bytem2 };
public const map<byte> bytem1 = { "key1": 10 };
public const map<byte> bytem2 = { "key2": 20 };

public const map<map<byte>> bytem3_new = { "key3": bytem1_new, "key4": bytem2_new };
public const map<byte> bytem1_new = { "key1": 10 };
public const map<byte> bytem2_new = { "key2": 20 };

public const map<map<byte>> bytem7 = { "key1": { "key3": 10 }, "key2": { "key4": 20 } };
public const map<map<byte>> bytem8 = { "key1": { "key3": 10 }, "key2": { "key4": 20 } };

// -----------------------------------------------------------

public const map<map<float>> fm3 = { "key3": fm1, "key4": fm2 };
public const map<float> fm1 = { "key1": 2.0 };
public const map<float> fm2 = { "key2": 4.0 };

public const map<map<float>> fm3_new = { "key3": fm1_new, "key4": fm2_new };
public const map<float> fm1_new = { "key1": 2.0 };
public const map<float> fm2_new = { "key2": 4.0 };

public const map<map<float>> fm7 = { "key1": { "key3": 2.0 }, "key2": { "key4": 4.0 } };
public const map<map<float>> fm8 = { "key1": { "key3": 2.0 }, "key2": { "key4": 4.0 } };

// -----------------------------------------------------------

public const map<map<decimal>> dm3 = { "key3": dm1, "key4": dm2 };
public const map<decimal> dm1 = { "key1": 100 };
public const map<decimal> dm2 = { "key2": 200 };

public const map<map<decimal>> dm3_new = { "key3": dm1_new, "key4": dm2_new };
public const map<decimal> dm1_new = { "key1": 100 };
public const map<decimal> dm2_new = { "key2": 200 };

public const map<map<decimal>> dm7 = { "key1": { "key3": 100 }, "key2": { "key4": 200 } };
public const map<map<decimal>> dm8 = { "key1": { "key3": 100 }, "key2": { "key4": 200 } };

// -----------------------------------------------------------

public const map<map<string>> sm3 = { "key3": sm1, "key4": sm2 };
public const map<string> sm1 = { "key1": "value1" };
public const map<string> sm2 = { "key2": "value2" };

public const map<map<string>> sm3_new = { "key3": sm1_new, "key4": sm2_new };
public const map<string> sm1_new = { "key1": "value1" };
public const map<string> sm2_new = { "key2": "value2" };

public const map<map<string>> sm7 = { "key1": { "key3": "value1" }, "key2": { "key4": "value2" } };
public const map<map<string>> sm8 = { "key1": { "key3": "value1" }, "key2": { "key4": "value2" } };

// -----------------------------------------------------------

public const map<map<()>> nm3 = { "key3": nm1, "key4": nm2 };
public const map<()> nm1 = { "key1": () };
public const map<()> nm2 = { "key2": () };

public const map<map<()>> nm3_new = { "key3": nm1_new, "key4": nm2_new };
public const map<()> nm1_new = { "key1": () };
public const map<()> nm2_new = { "key2": () };

public const map<map<()>> nm7 = { "key1": { "key3": () }, "key2": { "key4": () } };
public const map<map<()>> nm8 = { "key1": { "key3": () }, "key2": { "key4": () } };

// -----------------------------------------------------------

public const map<map<map<string>>> m3 = { "k3": m2 };

const map<map<string>> m2 = { "k2": m1 };

const map<string> m1 = { "k1": sVal };

const sVal = "v1";

public const sConst = "Ballerina";
public const iConst = 100;
public const map<string> mConst = { "mKey": "mValue" };

public type TestConfig record {
    string s;
    int i;
    map<string> m;
    !...;
};

public annotation<function> testAnnotation TestConfig;

public map<map<string>> m5 = { "m5k": m6 };
public const map<string> m6 = { "m6k": "m6v" };

public map<string>[] a1 = [m6];

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
