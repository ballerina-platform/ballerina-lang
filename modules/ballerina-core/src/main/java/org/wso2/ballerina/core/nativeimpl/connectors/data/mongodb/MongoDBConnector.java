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
package org.wso2.ballerina.core.nativeimpl.connectors.data.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ReadConcern;
import com.mongodb.ReadConcernLevel;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import org.bson.Document;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB data connector.
 */
@SuppressWarnings("rawtypes")
@BallerinaConnector(
        packageName = "ballerina.data.mongodb",
        connectorName = MongoDBConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "host", type = TypeEnum.STRING),
                @Argument(name = "database", type = TypeEnum.STRING),
                @Argument(name = "options", type = TypeEnum.MAP)
        })
@Component(
        name = "ballerina.data.connectors.mongodb",
        immediate = true,
        service = AbstractNativeConnector.class)
public class MongoDBConnector extends AbstractNativeConnector implements ServiceFactory {

    private static final String PACKAGE_NAME = "ballerina.data.sql";

    public static final String CONNECTOR_NAME = "Connector";
        
    private MongoClient client;
    
    private MongoDatabase db;
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean init(BValue[] bValueRefs) {
        String host = ((BString) bValueRefs[0]).stringValue();
        String dbName = ((BString) bValueRefs[1]).stringValue();
        if (bValueRefs.length > 2) {
            BMap<BString, BValue> options = ((BMap<BString, BValue>) bValueRefs[2]);
            this.client = new MongoClient(this.createServerAddresses(host), this.createOptions(options));
        } else {
            this.client = new MongoClient(this.createServerAddresses(host));
        }
        this.db = this.client.getDatabase(dbName);
        return true;
    }
    
    private MongoClientOptions createOptions(BMap<BString, BValue> options) {
        MongoClientOptions.Builder builder = MongoClientOptions.builder();
        BValue value = options.get(new BString(Constants.SSL_ENABLED));
        if (value != null) {
            builder = builder.sslEnabled(Boolean.parseBoolean(value.stringValue()));
        }
        value = options.get(new BString(Constants.READ_CONCERN));
        if (value != null) {
            builder = builder.readConcern(new ReadConcern(ReadConcernLevel.valueOf(value.stringValue())));
        }
        value = options.get(new BString(Constants.WRITE_CONCERN));
        if (value != null) {
            builder = builder.writeConcern(WriteConcern.valueOf(value.stringValue()));
        }
        value = options.get(new BString(Constants.READ_PREFERENCE));
        if (value != null) {
            builder = builder.readPreference((ReadPreference.valueOf(value.stringValue())));
        }
        value = options.get(new BString(Constants.SOCKET_TIMEOUT));
        if (value != null) {
            try {
                builder = builder.socketTimeout(Integer.parseInt(value.stringValue()));
            } catch (NumberFormatException e) {
                throw new BallerinaException("The socket timeout must be an integer value: " + 
                        value.stringValue(), e);
            }
        }
        value = options.get(new BString(Constants.CONNECTION_TIMEOUT));
        if (value != null) {
            try {
                builder = builder.connectTimeout(Integer.parseInt(value.stringValue()));
            } catch (NumberFormatException e) {
                throw new BallerinaException("The connection timeout must be an integer value: " + 
                        value.stringValue(), e);
            }
        }
        value = options.get(new BString(Constants.CONNECTIONS_PER_HOST));
        if (value != null) {
            try {
                builder = builder.connectionsPerHost(Integer.parseInt(value.stringValue()));
            } catch (NumberFormatException e) {
                throw new BallerinaException("Connections per host must be an integer value: " + 
                        value.stringValue(), e);
            }
        }
        return builder.build();
    }
    
    private List<ServerAddress> createServerAddresses(String hostStr) {
        List<ServerAddress> result = new ArrayList<>();
        String[] hosts = hostStr.split(",");
        for (String host : hosts) {
            result.add(this.createServerAddress(host));
        }
        return result;
    }
    
    private ServerAddress createServerAddress(String hostStr) {
        String[] hostPort = hostStr.split(":");
        String host = hostPort[0];
        int port;
        if (hostPort.length > 1) {
            try {
                port = Integer.parseInt(hostPort[2]);
            } catch (NumberFormatException e) {
                throw new BallerinaException("The port of the host string must be an integer: " + hostStr, e);
            }
        } else {
            port = ServerAddress.defaultPort();
        }
        return new ServerAddress(host, port);
    }
    
    @Override
    public String getPackageName() {
        return PACKAGE_NAME;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration serviceRegistration) {
        return new MongoDBConnector();
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object obj) {
    }

    @Override
    public AbstractNativeConnector getInstance() {
        return new MongoDBConnector();
    }
    
    public BJSON findOne(String collectionName, BJSON query) {
        MongoCollection<Document> collection = this.db.getCollection(collectionName);
        if (collection == null) {
            throw new BallerinaException("Invalid collection name: " + collectionName);
        }
        Document doc;
        if (query != null) {
            doc = collection.find(Document.parse(query.stringValue())).first();
        } else {
            doc = collection.find().first();
        }
        if (doc == null) {
            return null;
        } else {
            return new BJSON(doc.toJson());
        }
    }
    
    public BJSON find(String collectionName, BJSON query) {
        MongoCollection<Document> collection = this.db.getCollection(collectionName);
        if (collection == null) {
            throw new BallerinaException("Invalid collection name: " + collectionName);
        }
        MongoCursor<Document> itr;
        if (query != null) {
            itr = collection.find(Document.parse(query.stringValue())).iterator();
        } else {
            itr = collection.find().iterator();
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        while (itr.hasNext()) {
            builder.append(itr.next().toJson());
            if (itr.hasNext()) {
                builder.append(",");
            }
        }
        builder.append("]");
        return new BJSON(builder.toString());
    }
    
    public void insert(String collectionName, BJSON document) {
        MongoCollection<Document> collection = this.db.getCollection(collectionName);
        if (collection == null) {
            throw new BallerinaException("Invalid collection name: " + collectionName);
        }
        collection.insertOne(Document.parse(document.stringValue()));
    }
    
    public void insert(String collectionName, BArray<BJSON> documents) {
        MongoCollection<Document> collection = this.db.getCollection(collectionName);
        if (collection == null) {
            throw new BallerinaException("Invalid collection name: " + collectionName);
        }
        int count = documents.size();
        List<Document> docList = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            docList.add(Document.parse(documents.get(i).stringValue()));
        }
        collection.insertMany(docList);
    }
    
    public void update(String collectionName, BJSON filter, BJSON document, BBoolean multi, BBoolean upsert) {
        MongoCollection<Document> collection = this.db.getCollection(collectionName);
        if (collection == null) {
            throw new BallerinaException("Invalid collection name: " + collectionName);
        }
        UpdateOptions options = new UpdateOptions();
        options.upsert(upsert.booleanValue());
        if (multi.booleanValue()) {
            collection.updateMany(this.jsonToDoc(filter), this.jsonToDoc(document), options);
        } else {
            collection.updateOne(this.jsonToDoc(filter), this.jsonToDoc(document), options);
        }
    }
    
    public void delete(String collectionName, BJSON filter, BBoolean multi) {
        MongoCollection<Document> collection = this.db.getCollection(collectionName);
        if (collection == null) {
            throw new BallerinaException("Invalid collection name: " + collectionName);
        }
        if (multi.booleanValue()) {
            collection.deleteMany(this.jsonToDoc(filter));
        } else {
            collection.deleteOne(this.jsonToDoc(filter));
        }
    }
    
    public void close() {
        this.client.close();
    }
    
    private Document jsonToDoc(BJSON json) {
        return Document.parse(json.stringValue());
    }
    
}
