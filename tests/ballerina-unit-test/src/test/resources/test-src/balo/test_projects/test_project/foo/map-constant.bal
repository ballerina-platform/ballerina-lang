
public const map<map<boolean>> bm2 = { "key2": bm1 };
public const map<boolean> bm1 = { "key1": true };

public const map<map<int>> im2 = { "key2": im1 };
public const map<int> im1 = { "key1": 1 };

public const map<map<byte>> bytem2 = { "key2": bytem1 };
public const map<byte> bytem1 = { "key1": 10 };

public const map<map<float>> fm2 = { "key2": fm1 };
public const map<float> fm1 = { "key1": 2.0 };

public const map<map<decimal>> dm2 = { "key2": dm1 };
public const map<decimal> dm1 = { "key1": 100 };

public const map<map<string>> sm2 = { "key2": sm1 };
public const map<string> sm1 = { "key1": "value1" };

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

public map<map<string>> m4 = { "m4k": m5 };
public const map<string> m5 = { "m5k": "m5v" };

public map<string>[] a1 = [m5];

public const map<boolean> bm3 = { "bm3k": true };
public const map<boolean> bm4 = { "bm4kn": bm3.bm3k };

public const map<int> im3 = { "im3k": 123 };
public const map<int> im4 = { "im4kn": im3.im3k };

public const map<byte> bytem3 = { "bytem3k": 64 };
public const map<byte> bytem4 = { "bytem4kn": bytem3.bytem3k };

public const map<float> fm3 = { "fm3k": 12.5 };
public const map<float> fm4 = { "fm4kn": fm3.fm3k };

public const map<decimal> dm3 = { "dm3k": 5.56 };
public const map<decimal> dm4 = { "dm4kn": dm3.dm3k };

public const map<string> sm3 = { "sm3k": "sm3v" };
public const map<string> sm4 = { "sm4kn": sm3.sm3k };

public const map<()> nm3 = { "nm3k": () };
public const map<()> nm4 = { "nm4kn": nm3.nm3k };
