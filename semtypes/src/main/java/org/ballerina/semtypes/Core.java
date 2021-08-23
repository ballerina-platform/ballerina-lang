package org.ballerina.semtypes;

/**
 * Contains core functions for implementing semantic typing.
 *
 */
public class Core {

}

/**
 * SemType Interface.
 */
interface SemType {

}

/**
 * Complex SemType implementation.
 */
class ComplexSemType implements SemType {
    UniformTypeBitSet all;
    UniformTypeBitSet some;

}

/**
 * UniformTypeBitSet SemType implementation.
 */
class UniformTypeBitSet implements SemType {
    int value;

    public UniformTypeBitSet(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}