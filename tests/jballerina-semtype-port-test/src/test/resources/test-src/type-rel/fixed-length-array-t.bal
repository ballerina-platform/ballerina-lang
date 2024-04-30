type IntArray int[];

// @type Int5 < IntArray
type Int5 int[5];

// @type Int5 = Int5AndIntArray
// @type Int5AndIntArray < IntArray
type Int5AndIntArray Int5 & IntArray;

// @type IntArray <> ArrayOfIntArray
type ArrayOfIntArray int[][];

// @type ArrayOfInt5 < ArrayOfIntArray
// @type Int5 <> ArrayOfInt5
// @type Int5 = ArrayOfInt5[0]
// @type Int5 = ArrayOfInt5[5]
// @type Int5 = ArrayOfInt5[6]
type ArrayOfInt5 int[][5];

// @type Array5OfInt5 < ArrayOfInt5
// @type Array5OfInt5 < ArrayOfIntArray
type Array5OfInt5 int[5][5];

type INT int;

// @type Array5OfInt5 < Array5OfIntArray
// @type Array5OfIntArray < ArrayOfIntArray
// @type IntArray = Array5OfIntArray[0]
// @type IntArray = Array5OfIntArray[4]
type Array5OfIntArray int[5][];

type ROIntArray readonly & IntArray;

// @type ROInt5 < Int5
// @type ROInt5 < ROIntArray
type ROInt5 readonly & int[5];

// -@type ArrayExcept5 <> Int5;
// -@type ArrayExcept5 < IntArray;
type ArrayExcept5 IntArray & !Int5;

const FIVE = 5;

// @type ArrayOfInt5 = ArrayOfIntFive
type ArrayOfIntFive int[][FIVE];

// @type Array5OfInt5 = ArrayFiveOfIntFive
type ArrayFiveOfIntFive int[FIVE][FIVE];

// @type ROArrayFiveOfIntFive < ArrayFiveOfIntFive
// @type ROArrayFiveOfIntFive < Array5OfInt5
type ROArrayFiveOfIntFive ArrayFiveOfIntFive & readonly;

type N never;
// @type ArrayOfInt5 = TwoArraysOfInt5[0]
// @type ArrayOfInt5 = TwoArraysOfInt5[1]
// @type N = TwoArraysOfInt5[2]
type TwoArraysOfInt5 int[2][][5];

// @type EmptyIntArray < IntArray 
type EmptyIntArray int[0];

type Array2OfInt5 Int5[2];

type Array7OfArray2OfInt5 Array2OfInt5[7];

// @type Array7x2x5 = Array7OfArray2OfInt5
type Array7x2x5 int[7][2][5];
