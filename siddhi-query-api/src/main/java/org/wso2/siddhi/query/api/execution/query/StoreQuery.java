/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.query.api.execution.query;

import org.wso2.siddhi.query.api.execution.query.input.store.InputStore;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;

import java.io.Serializable;

/**
 * Siddhi Query
 */
public class StoreQuery implements Serializable {

    private static final long serialVersionUID = 1L;
    private InputStore inputStore;
    private Selector selector = new Selector();

    public static StoreQuery query() {
        return new StoreQuery();
    }

    public StoreQuery from(InputStore inputStore) {
        this.inputStore = inputStore;
        return this;
    }

    public InputStore getInputStore() {
        return inputStore;
    }

    public StoreQuery select(Selector selector) {
        this.selector = selector;
        return this;
    }

    public Selector getSelector() {
        return selector;
    }

    @Override
    public String toString() {
        return "StoreQuery{" +
                "inputStore=" + inputStore +
                ", selector=" + selector +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StoreQuery that = (StoreQuery) o;

        if (inputStore != null ? !inputStore.equals(that.inputStore) : that.inputStore != null) {
            return false;
        }
        return selector != null ? selector.equals(that.selector) : that.selector == null;
    }

    @Override
    public int hashCode() {
        int result = inputStore != null ? inputStore.hashCode() : 0;
        result = 31 * result + (selector != null ? selector.hashCode() : 0);
        return result;
    }
}
