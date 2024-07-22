// @type IsolatedFuncTop < FuncTop
type IsolatedFuncTop isolated function;

type FuncTop function;

// @type FI1 < F1
// @type FI1 < IsolatedFuncTop
// @type FI1 < FuncTop
type FI1 isolated function ();

type F1 function ();

type FI2 isolated function (int);

type FI3 isolated function (int, int);

// @type FI1 < FIX
// @type FI2 < FIX
// @type FI3 < FIX
// @type FIX < IsolatedFuncTop
// @type FIX < FuncTop
type FIX FI1|FI2|FI3;

type F2 function (int);

type F3 function (int, int);

// @type FIX < FX
type FX F1|F2|F3;

// @type F1 < FT1
type FT1 transactional function ();

type FT2 transactional function (int);

type FT3 transactional function (int, int);

// @type FT1 < FTX
// @type FT2 < FTX
// @type FT3 < FTX
// @type FX < FTX
// @type FIX < FTX
type FTX FT1|FT2|FT3;
