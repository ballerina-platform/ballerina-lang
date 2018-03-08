package org.ballerinalang.net.uri.parser;


import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * This class is use to set and return data in the template tree.
 * @param <DataType> Type of data which should be set and returned.
 */
public class DataReturnAgent<DataType> {

    private DataType data;
    private BallerinaException ballerinaException;

    /**
     * Set data.
     * @param data data which should get returned.
     */
    public void setData(DataType data) {
        this.data = data;
    }

    /**
     * Get data.
     * @return data stored in the Agent.
     */
    public DataType getData() {
        return data;
    }

    /**
     * Set Error.
     * @param ballerinaException the error to be set.
     */
    public void setError(BallerinaException ballerinaException) {
        this.ballerinaException = ballerinaException;
    }

    /**
     * Get Error.
     * @return the Throwable which caused the error.
     */
    public BallerinaException getError() {
        return ballerinaException;
    }
}
