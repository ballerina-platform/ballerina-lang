/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.nativeimpl.connectors.data.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BDataframe;
import org.wso2.ballerina.core.model.values.BDataframe.ColumnDefinition;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Cassandra data connector.
 */
@BallerinaConnector(
        packageName = "ballerina.data.cassandra",
        connectorName = CassandraConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "host", type = TypeEnum.STRING),
                @Argument(name = "options", type = TypeEnum.MAP)
        })
@Component(
        name = "ballerina.data.connectors.cassandra",
        immediate = true,
        service = AbstractNativeConnector.class)
public class CassandraConnector extends AbstractNativeConnector {

    private static final String PACKAGE_NAME = "ballerina.data.cassandra";

    public static final String CONNECTOR_NAME = "Connector";
    
    private Cluster cluster;
    
    private Session session;
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean init(BValue[] bValueRefs) {
        String hostStr = ((BString) bValueRefs[0]).stringValue();
        Cluster.Builder builder = Cluster.builder();
        builder.addContactPoints(hostStr.split(",")).build();
        if (bValueRefs.length > 1) {
            BMap<BString, BValue> options = ((BMap<BString, BValue>) bValueRefs[1]);
            builder = this.populateOptions(builder, options);
        }
        this.cluster = builder.build();
        this.session = this.cluster.connect();
        return true;
    }
    
    private Cluster.Builder populateOptions(Cluster.Builder builder, BMap<BString, BValue> options) {
        BValue value = options.get(new BString(Constants.SSL_ENABLED));
        if (value != null && Boolean.parseBoolean(value.stringValue())) {
            builder = builder.withSSL();
        }
        QueryOptions queryOpts = new QueryOptions();
        value = options.get(new BString(Constants.CONSISTENCY_LEVEL));
        if (value != null) {
            queryOpts.setConsistencyLevel(ConsistencyLevel.valueOf(value.stringValue()));
        }
        value = options.get(new BString(Constants.FETCH_SIZE));
        if (value != null) {
            try {
                queryOpts.setFetchSize(Integer.parseInt(value.stringValue()));
            } catch (NumberFormatException e) {
                throw new BallerinaException("Fetch size must be an integer value: " + 
                        value.stringValue(), e);
            }
        }
        return builder.withQueryOptions(queryOpts);
    }
    
    @Override
    public String getPackageName() {
        return PACKAGE_NAME;
    }

    @Override
    public AbstractNativeConnector getInstance() {
        return new CassandraConnector();
    }
    
    public BDataframe execute(String query, Object[] args) {
        ResultSet rs = this.session.execute(query, args);
        return new BDataframe(new CassandraDataIterator(rs), new HashMap<>(), this.extractColumnDefs(rs));
    }
    
    private List<ColumnDefinition> extractColumnDefs(ResultSet rs) {
        List<ColumnDefinition> columnDefs = new ArrayList<>();
        for (Definition def : rs.getColumnDefinitions().asList()) {
            columnDefs.add(new ColumnDefinition(def.getName(), this.convert(def.getType())));
        }
        return columnDefs;
    }
    
    private TypeEnum convert(DataType type) {
        if (DataType.ascii().equals(type)) {
            return TypeEnum.STRING;
        } else if (DataType.bigint().equals(type)) {
            return TypeEnum.LONG;
        } else if (DataType.blob().equals(type)) {
            return TypeEnum.STRING;
        } else if (DataType.cboolean().equals(type)) {
            return TypeEnum.BOOLEAN;
        } else if (DataType.cdouble().equals(type)) {
            return TypeEnum.DOUBLE;
        } else if (DataType.cfloat().equals(type)) {
            return TypeEnum.FLOAT;
        } else if (DataType.cint().equals(type)) {
            return TypeEnum.INT;
        } else if (DataType.counter().equals(type)) {
            return TypeEnum.INT;
        } else if (DataType.date().equals(type)) {
            return TypeEnum.LONG;
        } else if (DataType.decimal().equals(type)) {
            return TypeEnum.DOUBLE;
        } else if (DataType.smallint().equals(type)) {
            return TypeEnum.INT;
        } else if (DataType.text().equals(type)) {
            return TypeEnum.STRING;
        } else if (DataType.time().equals(type)) {
            return TypeEnum.LONG;
        } else if (DataType.timestamp().equals(type)) {
            return TypeEnum.LONG;
        } else if (DataType.tinyint().equals(type)) {
            return TypeEnum.INT;
        } else if (DataType.uuid().equals(type)) {
            return TypeEnum.STRING;
        } else if (DataType.varchar().equals(type)) {
            return TypeEnum.STRING;
        } else if (DataType.varint().equals(type)) {
            return TypeEnum.LONG;
        } else {
            return TypeEnum.STRING;
        }
    }
    
    public void close() {
        this.session.close();
        this.cluster.close();
    }
    
}
