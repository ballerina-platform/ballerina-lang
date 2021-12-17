// Array2OfInt5<:Array5OfIntArray
// Array2OfInt5<:ArrayOfIntArray
// Array5OfInt5<:Array5OfIntArray
// Array5OfInt5<:ArrayFiveOfIntFive
// Array5OfInt5<:ArrayOfInt5
// Array5OfInt5<:ArrayOfIntArray
// Array5OfInt5<:ArrayOfIntFive
// Array5OfIntArray<:ArrayOfIntArray
// ArrayFiveOfIntFive<:Array5OfInt5
// ArrayFiveOfIntFive<:Array5OfIntArray
// ArrayFiveOfIntFive<:ArrayOfInt5
// ArrayFiveOfIntFive<:ArrayOfIntArray
// ArrayFiveOfIntFive<:ArrayOfIntFive
// ArrayOfInt5<:ArrayOfIntArray
// ArrayOfInt5<:ArrayOfIntFive
// ArrayOfIntFive<:ArrayOfInt5
// ArrayOfIntFive<:ArrayOfIntArray
// EmptyIntArray<:Array5OfIntArray
// EmptyIntArray<:ArrayOfIntArray
// EmptyIntArray<:IntArray
// FIVE<:INT
// Int5<:IntArray
// N<:Array2OfInt5
// N<:Array5OfInt5
// N<:Array5OfIntArray
// N<:Array7OfArray2OfInt5
// N<:Array7x2x5
// N<:ArrayFiveOfIntFive
// N<:ArrayOfInt5
// N<:ArrayOfIntArray
// N<:ArrayOfIntFive
// N<:EmptyIntArray
// N<:FIVE
// N<:INT
// N<:Int5
// N<:IntArray
// N<:ROArrayFiveOfIntFive
// N<:ROInt5
// N<:ROIntArray
// N<:TwoArraysOfInt5
// ROArrayFiveOfIntFive<:Array5OfInt5
// ROArrayFiveOfIntFive<:Array5OfIntArray
// ROArrayFiveOfIntFive<:ArrayFiveOfIntFive
// ROArrayFiveOfIntFive<:ArrayOfInt5
// ROArrayFiveOfIntFive<:ArrayOfIntArray
// ROArrayFiveOfIntFive<:ArrayOfIntFive
// ROInt5<:Int5
// ROInt5<:IntArray
// ROInt5<:ROIntArray
// ROIntArray<:IntArray

type IntArray int[];

type Int5 int[5];

type ArrayOfIntArray int[][];

type ArrayOfInt5 int[][5];

type Array5OfInt5 int[5][5];

type INT int;


type Array5OfIntArray int[5][];

type ROIntArray readonly & IntArray;

type ROInt5 readonly & int[5];

const FIVE = 5;

type ArrayOfIntFive int[][FIVE];

type ArrayFiveOfIntFive int[FIVE][FIVE];

type ROArrayFiveOfIntFive ArrayFiveOfIntFive & readonly;

type N never;

type TwoArraysOfInt5 int[2][][5];

type EmptyIntArray int[0];

type Array2OfInt5 Int5[2];

type Array7OfArray2OfInt5 Array2OfInt5[7];

type Array7x2x5 int[7][2][5];