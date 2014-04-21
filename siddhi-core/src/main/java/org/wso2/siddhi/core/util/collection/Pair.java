package org.wso2.siddhi.core.util.collection;

import java.io.Serializable;


public final class Pair<One, Two> implements Serializable {

    private One one;
    private Two two;

    public Pair(One one, Two two) {
        this.one = one;
        this.two = two;
    }

    public int hashCode() {
        return one.hashCode() ^ two.hashCode();

    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Pair) {
            Pair pairObject = (Pair) object;

            return (one == null ?
                    pairObject.one == null : one.equals(pairObject.one)) &&
                   (two == null ?
                    pairObject.two == null : pairObject.equals(pairObject.two));
        }

        return false;
    }


    public One getOne() {
        return one;
    }

    public void setOne(One one) {
        this.one = one;
    }

    public Two getTwo() {
        return two;
    }

    public void setTwo(Two two) {
        this.two = two;
    }
}
