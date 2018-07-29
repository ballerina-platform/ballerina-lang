package org.ballerinalang.bir.model;

import org.ballerinalang.bir.model.op.OpGoTo;
import org.ballerinalang.bir.model.op.OpReturn;

/**
 * Abstract visitor to iterate though op codes.
 *
 * @param <T>
 */
public interface OpVisitor<T> {

    void accept(OpGoTo opGoTo);

    void accept(OpReturn opReturn);
}
