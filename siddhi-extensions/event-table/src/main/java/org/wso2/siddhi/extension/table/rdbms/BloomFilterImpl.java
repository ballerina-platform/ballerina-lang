/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.table.rdbms;


import org.apache.hadoop.util.bloom.CountingBloomFilter;
import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BloomFilterImpl {

    private CountingBloomFilter[] bloomFilters;
    private int bloomFilterSize;
    private int bloomFilterHashFunction;
    private List<Attribute> attributeList;

    public BloomFilterImpl(int bloomFilterSize, int bloomFilterHashFunction, List<Attribute> attributeList) {
        this.bloomFilterSize = bloomFilterSize;
        this.bloomFilterHashFunction = bloomFilterHashFunction;
        this.attributeList = attributeList;
    }

    public void buildBloomFilters(ResultSet results) {
        CountingBloomFilter[] bloomFilters = new CountingBloomFilter[attributeList.size()];
        for (int i = 0; i < bloomFilters.length; i++) {
            bloomFilters[i] = new CountingBloomFilter(bloomFilterSize, bloomFilterHashFunction, Hash.MURMUR_HASH);
        }

        try {
            while (results.next()) {
                for (int i = 0; i < bloomFilters.length; i++) {
                    switch (attributeList.get(i).getType()) {
                        case INT:
                            bloomFilters[i].add(new Key(Integer.toString(results.getInt(i + 1)).getBytes()));
                            break;
                        case LONG:
                            bloomFilters[i].add(new Key(Long.toString(results.getLong(i + 1)).getBytes()));
                            break;
                        case FLOAT:
                            bloomFilters[i].add(new Key(Float.toString(results.getFloat(i + 1)).getBytes()));
                            break;
                        case DOUBLE:
                            bloomFilters[i].add(new Key(Double.toString(results.getDouble(i + 1)).getBytes()));
                            break;
                        case STRING:
                            String attributeValue = results.getString(i + 1);
                            if (attributeValue != null) {
                                bloomFilters[i].add(new Key(attributeValue.getBytes()));
                            }
                            break;
                        case BOOL:
                            bloomFilters[i].add(new Key(Boolean.toString(results.getBoolean(i + 1)).getBytes()));
                            break;

                    }
                }
            }
            results.close();
            this.bloomFilters = bloomFilters;
        } catch (SQLException ex) {
            throw new SiddhiAppRuntimeException("Error while initiating blooms filter with db data, " + ex
                    .getMessage(), ex);
        }
    }

    public void addToBloomFilters(ComplexEvent event) {
        for (int i = 0; i < attributeList.size(); i++) {
            if (event.getOutputData()[i] != null) {
                bloomFilters[i].add(new Key(event.getOutputData()[i].toString().getBytes()));
            }
        }
    }

    public void addToBloomFilters(Object[] obj) {
        for (int i = 0; i < attributeList.size(); i++) {
            if (obj[i] != null) {
                bloomFilters[i].add(new Key(obj[i].toString().getBytes()));
            }
        }
    }

    public void removeFromBloomFilters(Object[] obj) {
        for (int i = 0; i < attributeList.size(); i++) {
            if (obj[i] != null) {
                bloomFilters[i].delete(new Key(obj[i].toString().getBytes()));
            }
        }
    }

    public CountingBloomFilter[] getBloomFilters() {
        return bloomFilters;
    }

}
