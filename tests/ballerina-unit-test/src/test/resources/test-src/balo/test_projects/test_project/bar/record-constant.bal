
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
