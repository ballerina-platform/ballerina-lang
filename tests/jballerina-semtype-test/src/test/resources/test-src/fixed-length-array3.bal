// ROInt5<:IntArray
// ROInt5<:Int5
// ROInt5<:ROIntArray
// Int5<:IntArray
// ROIntArray<:IntArray

type ROIntArray readonly & IntArray;

type ROInt5 readonly & int[5];

type IntArray int[];

type INT int;

type Int5 int[5];
