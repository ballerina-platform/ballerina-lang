// @type DistinctObject1 < ObjectTy1
// @type DistinctObject2 < ObjectTy1
// @type DistinctObject1 <> DistinctObject2
// @type DistinctObject3 < DistinctObject1

type DistinctObject1 distinct ObjectTy1;

type DistinctObject2 distinct ObjectTy1;

type DistinctObject3 distinct DistinctObject1;

type ObjectTy1 object {
    int foo;
    function bar() returns int;
};

// @type RecursiveDistinctObject1 < RecursiveObject
// @type RecursiveDistinctObject1 <> RecursiveDistinctObject2
type RecursiveDistinctObject1 distinct object {
    RecursiveDistinctObject1? oo;
};

type RecursiveDistinctObject2 distinct RecursiveObject;

type RecursiveObject object {
    RecursiveObject? oo;
};
