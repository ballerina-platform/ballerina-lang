import ballerina.collections;

function testAdd (string[] values) (collections:Vector) {
    collections:Vector vector = {vec:[]};
    int i = 0;

    while (i < lengthof values) {
        vector.add(values[i]);
        i = i + 1;
    }

    return vector;
}

function testGet (int size) (any x1, any x2, any x3, any x4) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);

    x1, _ = vector.get(0);
    x2, _ = vector.get(2);
    x3, _ = vector.get(4);
    x4, _ = vector.get(6);

    return;
}

function testInsert (int[] values, int[] indices, int size) (collections:Vector) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);

    int i = 0;
    while (i < lengthof values) {
        _ = vector.insert(values[i], indices[i]);
        i = i + 1;
    }

    return vector;
}

function testRemove (int size) (collections:Vector vector, any x1, any x2, any x3) {
    vector = {vec:[]};
    populateVector(vector, size);

    x1, _ = vector.remove(0);
    x2, _ = vector.remove(2);
    x3, _ = vector.remove(4);

    return;
}

function testGetIndexOutOfRange(int size) (any x1, any x2, any x3, any x4, collections:IndexOutOfRangeError e1, collections:IndexOutOfRangeError e2) {
    collections:Vector vector = {vec:[]};
    populateVector(vector, size);

    x1, _ = vector.get(0);
    x2, _ = vector.get(2);
    x3, e1 = vector.get(15);
    x4, e2 = vector.get(-1);

    return;
}

function testInsertIndexOutOfRange(int[] values, int[] indices, int size) (collections:Vector vector, collections:IndexOutOfRangeError[] errs) {
    vector = {vec:[]};
    populateVector(vector, size);
    errs = [];

    int i = 0;
    while (i < lengthof values) {
        errs[i] = vector.insert(values[i], indices[i]);
        i = i + 1;
    }

    return vector, errs;
}

function testRemoveIndexOutOfRange (int size) (collections:Vector vector, any x1, any x2, any x3, collections:IndexOutOfRangeError[] errs) {
    vector = {vec:[]};
    populateVector(vector, size);
    errs = [];

    x1, errs[0] = vector.remove(0);
    x2, errs[1] = vector.remove(20);
    x3, errs[2] = vector.remove(-1);

    return;
}

function populateVector (collections:Vector vector, int size) {
    int i = 1;
    while (i <= size) {
        vector.add(i * 10);
        i = i + 1;
    }
}