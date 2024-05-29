// @type NegativeZero < Float
// @type Zero < Float
// @type NegativeZero = Zero

type Zero 0.0;
type NegativeZero -0.0;

type Float float;

// @type D1 <> Float
// @type D1 <> Zero
type D1 0.0d;

// @type F1 < Float
// @type F1 = Zero
// @type F1 = NegativeZero
// @type F2 = Zero
// @type F2 = NegativeZero
type F1 Zero|NegativeZero;
type F2 F1 & Zero;
