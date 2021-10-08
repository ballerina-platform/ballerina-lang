// X<:S
// X<:XY
// X<:XYZ
// X<:XZ
// XY<:S
// XY<:XYZ
// XYZ<:S
// XZ<:S
// XZ<:XYZ
// Y<:S
// Y<:XY
// Y<:XYZ
// Z<:S
// Z<:XYZ
// Z<:XZ
type X "x";
type Y "y";
type Z "z";

type XYZ X|Y|Z;
type XY X|Y;
type XZ X|Z;

type S string;
