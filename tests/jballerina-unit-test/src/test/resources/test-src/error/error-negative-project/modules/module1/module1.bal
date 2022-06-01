public type E1 distinct error;

public type E2 distinct error;

type E3 distinct error;

type E4 distinct error;

type E5 E1 & E2;

type E6 E3 & E4;

type E7 E1 & E4;

type E8 E1|E2;

type E9 E3|E4;

type E10 E1|E4;

public type E5_Public E1 & E2;

public type E6_Public E3 & E4;

public type E7_Public E1 & E4;

public type E8_Public E1|E2;

public type E9_Public E3|E4;

public type E10_Public E1|E4;
