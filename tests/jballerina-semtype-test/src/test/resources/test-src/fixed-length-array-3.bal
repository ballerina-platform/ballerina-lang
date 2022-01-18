// ArrayFiveOfIntFive<:ArrayOfIntFive
// ROArrayFiveOfIntFive<:ArrayOfIntFive
// ROArrayFiveOfIntFive<:ArrayFiveOfIntFive

const FIVE = 5;

type ArrayOfIntFive int[][FIVE];

type ArrayFiveOfIntFive int[FIVE][FIVE];

type ROArrayFiveOfIntFive ArrayFiveOfIntFive & readonly;

type INT int;
