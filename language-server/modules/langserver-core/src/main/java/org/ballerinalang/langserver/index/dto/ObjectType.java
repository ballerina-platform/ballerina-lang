package org.ballerinalang.langserver.index.dto;

/**
 * Created by nadeeshaan on 6/9/18.
 */
public enum ObjectType {
    ENDPOINT(1),
    ACTION_HOLDER(2),
    OBJECT(3);

    private int type;

    ObjectType(int type) {
        this.type = type;
    }

    public int getValue() {
        return this.type;
    }
}
