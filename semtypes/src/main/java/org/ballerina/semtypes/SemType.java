package org.ballerina.semtypes;

/**
 * Contains core functions for implementing semantic typing.
 *
 */
class SemtypePlaceholder {
    SemType s1;
    SemType s2;
    public SemtypePlaceholder() {
        s1 = new ComplexSemType();
        s2 = new UniformTypeBitSet(0x00);
    }
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
