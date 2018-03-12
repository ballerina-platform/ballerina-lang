
package org.test;

public enum kind {
    PLUS,
    MINUS,
    AND,
    OR,
    NOT,
    GREATER_THAN
}


public function getEnumeratorInPackage() returns (kind) {
    return kind.NOT;
}

