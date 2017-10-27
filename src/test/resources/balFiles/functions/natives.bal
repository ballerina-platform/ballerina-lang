import ballerina.doc;

@doc:Description {value:"Native function ballerina.model.arrays:copyOf(double[])"}
@doc:Param {value:"arr: Array to copy" }
native function copyOf (float[] floatArray) (float[]);