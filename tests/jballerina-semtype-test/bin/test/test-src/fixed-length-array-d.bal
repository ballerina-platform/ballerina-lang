// @type Int5 = Int5AndIntArray
// @type Int5AndIntArray < IntArray
type Int5AndIntArray Int5 & IntArray;

// @type Int5 = ArrayOfInt5[0]
// @type Int5 = ArrayOfInt5[5]
// @type Int5 = ArrayOfInt5[6]
type ArrayOfInt5 int[][5];

// @type IntArray = Array5OfIntArray[0]
// @type IntArray = Array5OfIntArray[4]
type Array5OfIntArray int[5][];

// @type ArrayExcept5 <> Int5;
// @type ArrayExcept5 < IntArray;
type ArrayExcept5 IntArray & !Int5;

// @type ArrayOfInt5 = TwoArraysOfInt5[0]
// @type ArrayOfInt5 = TwoArraysOfInt5[1]
// @type N = TwoArraysOfInt5[2]
type TwoArraysOfInt5 int[2][][5];

type IntArray int[];

type Int5 int[5];
