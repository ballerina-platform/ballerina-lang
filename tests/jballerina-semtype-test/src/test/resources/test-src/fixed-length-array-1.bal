// Int5<:IntArray
// ROIntArray<:IntArray
// ROInt5<:IntArray
// ROInt5<:Int5
// ROInt5<:ROIntArray

type IntArray int[];

type Int5 int[5];

type ROIntArray readonly & IntArray;

type ROInt5 readonly & int[5];

type ArrayOfInt5 int[][5];

type INT int;
