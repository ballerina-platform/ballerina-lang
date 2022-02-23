// ArrayFiveOfIntFive<:ArrayOfIntFive
// ROArrayFiveOfIntFive<:ArrayOfIntFive
// Array5OfInt5<:ArrayOfIntFive
// ROArrayFiveOfIntFive<:ArrayFiveOfIntFive
// ArrayFiveOfIntFive<:Array5OfInt5
// Array5OfInt5<:ArrayFiveOfIntFive
// ROArrayFiveOfIntFive<:Array5OfInt5

const FIVE = 5;

type ArrayOfIntFive int[][FIVE];

type ArrayFiveOfIntFive int[FIVE][FIVE];

type ROArrayFiveOfIntFive ArrayFiveOfIntFive & readonly;

type INT int;

type Array5OfInt5 int[5][5];
