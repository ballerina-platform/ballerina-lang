type IS16 int:Signed16;
type I int;
type I2 int?;

// @type T1 <> T2
type T1 [int, string];
type T2 [string, int];

// @type T4 < T3
// @type T3[0] = I2
// @type T3[1] = I
// @type T4[1] = IS16
type T3 [int?, int, any];
type T4 [int, int:Signed16, int];
