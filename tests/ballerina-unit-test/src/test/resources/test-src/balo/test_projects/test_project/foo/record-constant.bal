
public const record {| record {| boolean...; |}...; |} br3 = { key5: br1, key6: br2, key7: { key8: true, key9: false }};
public const record {| boolean...; |} br1 = { key1: true, key2: false };
public const record {| boolean...; |} br2 = { key3: false, key4: true };

public const record {| record {| boolean...; |}...; |} br3_new = { key5: br1_new, key6: br2_new, key7: { key8: true, key9: false }};
public const record {| boolean...; |} br1_new = { key1: true, key2: false };
public const record {| boolean...; |} br2_new = { key3: false, key4: true };

public const record {| record {| boolean...; |}...; |} br7 = { key5: { key1: true, key2: false }, key6: { key3: false, key4: true }, key7: { key8: true, key9: false }};
public const record {| record {| boolean...; |}...; |} br8 = { key5: { key1: true, key2: false }, key6: { key3: false, key4: true }, key7: { key8: true, key9: false }};

// -----------------------------------------------------------

public const record {| record {| int...; |}...; |} ir3 = { key5: ir1, key6: ir2, key7: { key8: 8, key9: 9 }};
public const record {| int...; |} ir1 = { key1: 1, key2: 2 };
public const record {| int...; |} ir2 = { key3: 3, key4: 4 };

public const record {| record {| int...; |}...; |} ir3_new = { key5: ir1_new, key6: ir2_new, key7: { key8: 8, key9: 9 }};
public const record {| int...; |} ir1_new = { key1: 1, key2: 2 };
public const record {| int...; |} ir2_new = { key3: 3, key4: 4 };

public const record {| record {| int...; |}...; |} ir7 = { key5: { key1: 1, key2: 2 }, key6: { key3: 3, key4: 4 }, key7: { key8: 8, key9: 9 }};
public const record {| record {| int...; |}...; |} ir8 = { key5: { key1: 1, key2: 2 }, key6: { key3: 3, key4: 4 }, key7: { key8: 8, key9: 9 }};

// -----------------------------------------------------------

public const record {| record {| byte...; |}...; |} byter3 = { key5: byter1, key6: byter2, key7: { key8: 80, key9: 90 }};
public const record {| byte...; |} byter1 = { key1: 10, key2: 20 };
public const record {| byte...; |} byter2 = { key3: 30, key4: 40 };

public const record {| record {| byte...; |}...; |} byter3_new = { key5: byter1_new, key6: byter2_new, key7: { key8: 80, key9: 90 }};
public const record {| byte...; |} byter1_new = { key1: 10, key2: 20 };
public const record {| byte...; |} byter2_new = { key3: 30, key4: 40 };

public const record {| record {| byte...; |}...; |} byter7 = { key5: { key1: 10, key2: 20 }, key6: { key3: 30, key4: 40 }, key7: { key8: 80, key9: 90 }};
public const record {| record {| byte...; |}...; |} byter8 = { key5: { key1: 10, key2: 20 }, key6: { key3: 30, key4: 40 }, key7: { key8: 80, key9: 90 }};

// -----------------------------------------------------------

public const record {| record {| float...; |}...; |} fr3 = { key5: fr1, key6: fr2, key7: { key8: 8.0, key9: 9.0 }};
public const record {| float...; |} fr1 = { key1: 1.0, key2: 2.0 };
public const record {| float...; |} fr2 = { key3: 3.0, key4: 4.0 };

public const record {| record {| float...; |}...; |} fr3_new = { key5: fr1_new, key6: fr2_new, key7: { key8: 8.0, key9: 9.0 }};
public const record {| float...; |} fr1_new = { key1: 1.0, key2: 2.0 };
public const record {| float...; |} fr2_new = { key3: 3.0, key4: 4.0 };

public const record {| record {| float...; |}...; |} fr7 = { key5: { key1: 1.0, key2: 2.0 }, key6: { key3: 3.0, key4: 4.0 }, key7: { key8: 8.0, key9: 9.0 }};
public const record {| record {| float...; |}...; |} fr8 = { key5: { key1: 1.0, key2: 2.0 }, key6: { key3: 3.0, key4: 4.0 }, key7: { key8: 8.0, key9: 9.0 }};

// -----------------------------------------------------------

public const record {| record {| decimal...; |}...; |} dr3 = { key5: dr1, key6: dr2, key7: { key8: 800, key9: 900 }};
public const record {| decimal...; |} dr1 = { key1: 100, key2: 200 };
public const record {| decimal...; |} dr2 = { key3: 300, key4: 400 };

public const record {| record {| decimal...; |}...; |} dr3_new = { key5: dr1_new, key6: dr2_new, key7: { key8: 800, key9: 900 }};
public const record {| decimal...; |} dr1_new = { key1: 100, key2: 200 };
public const record {| decimal...; |} dr2_new = { key3: 300, key4: 400 };

public const record {| record {| decimal...; |}...; |} dr7 = { key5: { key1: 100, key2: 200 }, key6: { key3: 300, key4: 400 }, key7: { key8: 800, key9: 900 }};
public const record {| record {| decimal...; |}...; |} dr8 = { key5: { key1: 100, key2: 200 }, key6: { key3: 300, key4: 400 }, key7: { key8: 800, key9: 900 }};

// -----------------------------------------------------------

public const record {| record {| string...; |}...; |} sr3 = { key5: sr1, key6: sr2, key7: { key8: "value8", key9: "value9" }};
public const record {| string...; |} sr1 = { key1: "value1", key2: "value2" };
public const record {| string...; |} sr2 = { key3: "value3", key4: "value4" };

public const record {| record {| string...; |}...; |} sr3_new = { key5: sr1_new, key6: sr2_new, key7: { key8: "value8", key9: "value9" }};
public const record {| string...; |} sr1_new = { key1: "value1", key2: "value2" };
public const record {| string...; |} sr2_new = { key3: "value3", key4: "value4" };

public const record {| record {| string...; |}...; |} sr7 = { key5: { key1: "value1", key2: "value2" }, key6: { key3: "value3", key4: "value4" }, key7: { key8: "value8", key9: "value9" }};
public const record {| record {| string...; |}...; |} sr8 = { key5: { key1: "value1", key2: "value2" }, key6: { key3: "value3", key4: "value4" }, key7: { key8: "value8", key9: "value9" }};

// -----------------------------------------------------------

public const record {| record {| ()...; |}...; |} nr3 = { key5: nr1, key6: nr2, key7: { key8: (), key9: () }};
public const record {| ()...; |} nr1 = { key1: (), key2: () };
public const record {| ()...; |} nr2 = { key3: (), key4: () };

public const record {| record {| ()...; |}...; |} nr3_new = { key5: nr1_new, key6: nr2_new, key7: { key8: (), key9: () }};
public const record {| ()...; |} nr1_new = { key1: (), key2: () };
public const record {| ()...; |} nr2_new = { key3: (), key4: () };

public const record {| record {| ()...; |}...; |} nr7 = { key5: { key1: (), key2: () }, key6: { key3: (), key4: () }, key7: { key8: (), key9: () }};
public const record {| record {| ()...; |}...; |} nr8 = { key5: { key1: (), key2: () }, key6: { key3: (), key4: () }, key7: { key8: (), key9: () }};

// -----------------------------------------------------------

public const record {| record {| record {| string...; |}...; |}...; |} r3 = { k3: r2 };

const record {| record {| string...; |}...; |} r2 = { k2: r1 };

const record {| string...; |} r1 = { k1: srVal };

const srVal = "v1";

public const srConst = "Ballerina";
public const irConst = 100;
public const record {| string...; |} mrConst = { mKey: "mValue" };

public type RTestConfig record {|
    string s;
    int i;
    record {| string...; |} m;
|};

public annotation<function> recordTestAnnotation RTestConfig;

record {| record {| string...; |}...; |} r5 = { r5k: r6 };
public const record {| string...; |} r6 = { r6k: "r6v" };

function getR5() returns record {| record {| string...; |}...; |} {
    return r5;
}

record {| string...; |}[] ra1 = [r6];

function getRA1() returns record {| string...; |}[] {
    return ra1;
}

public const record {| boolean...; |} br4  = { br4k: true };
public const record {| boolean...; |} br5 = { br5kn: br4.br4k };

public const record {| int...; |} ir4 = { ir4k: 123 };
public const record {| int...; |} ir5 = { ir5kn: ir4.ir4k };

public const record {| byte...; |} byter4 = { byter4k: 64 };
public const record {| byte...; |} byter5 = { byter5kn: byter4.byter4k };

public const record {| float...; |} fr4 = { fr4k: 12.5 };
public const record {| float...; |} fr5 = { fr5kn: fr4.fr4k };

public const record {| decimal...; |} dr4 = { dr4k: 5.56 };
public const record {| decimal...; |} dr5 = { dr5kn: dr4.dr4k };

public const record {| string...; |} sr4 = { sr4k: "sr4v" };
public const record {| string...; |} sr5 = { sr5kn: sr4.sr4k };

public const record {| ()...; |} nr4 = { nr4k: () };
public const record {| ()...; |} nr5 = { nr5kn: nr4.nr4k };

// -----------------------------------------------------------

record {| record {| boolean...; |}...; |} br10 = { br10k: br11 };
public const record {| boolean...; |} br11 = { br11k: true };

public function getBR10() returns record {| record {| boolean...; |}...; |} {
    return br10;
}

record {| boolean...; |}[] bra1 = [br11];

public function getBRA1() returns record {| boolean...; |}[] {
    return bra1;
}

record {| record {| int...; |}...; |} ir10 = { ir10k: ir11 };
public const record {| int...; |} ir11 = { ir11k: 10 };

public function getIR10() returns record {| record {| int...; |}...; |} {
    return ir10;
}

record {| int...; |}[] ira1 = [ir11];

public function getIRA1() returns record {| int...; |}[] {
    return ira1;
}

record {| record {| byte...; |}...; |} byter10 = { byter10k: byter11 };
public const record {| byte...; |} byter11 = { byter11k: 4 };

public function getBYTER10() returns record {| record {| byte...; |}...; |} {
    return byter10;
}

record {| byte...; |}[] bytera1 = [byter11];

public function getBYTERA1() returns record {| byte...; |}[] {
    return bytera1;
}

record {| record {| float...; |}...; |} fr10 = { fr10k: fr11 };
public const record {| float...; |} fr11 = { fr11k: 40.0 };

public function getFR10() returns record {| record {| float...; |}...; |} {
    return fr10;
}

record {| float...; |}[] fra1 = [fr11];

public function getFRA1() returns record {| float...; |}[] {
    return fra1;
}

record {| record {| decimal...; |}...; |} dr10 = { dr10k: dr11 };
public const record {| decimal...; |} dr11 = { dr11k: 125 };

public function getDRM10() returns record {| record {| decimal...; |}...; |} {
    return dr10;
}

record {| decimal...; |}[] dra1 = [dr11];

public function getDRA1() returns record {| decimal...; |}[] {
    return dra1;
}

record {| record {| string...; |}...; |} sr10 = { sr10k: sr11 };
public const record {| string...; |} sr11 = { sr11k: "sr11v" };

public function getSR10() returns record {| record {| string...; |}...; |} {
    return sr10;
}

record {| string...; |}[] sra1 = [sr11];

public function getSRA1() returns record {| string...; |}[] {
    return sra1;
}

record {| record {| ()...; |}...; |} nr10 = { nr10k: nr11 };
public const record {| ()...; |} nr11 = { nr11k: () };

public function getNR10() returns record {| record {| ()...; |}...; |} {
    return nr10;
}

record {| ()...; |}[] nra1 = [nr11];

public function getNRA1() returns record {| ()...; |}[] {
    return nra1;
}
