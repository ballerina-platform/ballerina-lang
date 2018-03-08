package org.ballerinalang.net.uri.parser;


/**
 * This class is use to set and return data in the template tree.
 * @param <DataType> Type of data which should be set and returned.
 */
public class DataReturnAgent<DataType> {

    private DataType data;

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
}
