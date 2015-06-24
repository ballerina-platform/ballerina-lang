/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.extension.ml;

import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.Feature;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.impl.MLIOFactory;
import org.wso2.carbon.ml.core.impl.Predictor;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelHandler {

    public static final String REGISTRY_STORAGE_PREFIX = "registry";
    public static final String PATH_TO_GOVERNANCE_REGISTRY = "/_system/governance";

    private MLModel mlModel;
    private long modelId;

    /**
     *
     * @param modelStorageLocation MLModel storage location
     * @throws ClassNotFoundException
     * @throws java.net.URISyntaxException
     * @throws MLInputAdapterException
     * @throws java.io.IOException
     */
    public ModelHandler(String modelStorageLocation)
            throws ClassNotFoundException, URISyntaxException, MLInputAdapterException, IOException {
        mlModel = retrieveModel(modelStorageLocation);
    }

    /**
     * Retrieve the MLModel from the storage location
     * @param modelStorageLocation model storage location (file path or registry path)
     *                             file: -> file path
     *                             registry: -> registry path
     * @return
     * @throws URISyntaxException
     * @throws MLInputAdapterException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static MLModel retrieveModel(String modelStorageLocation)
            throws URISyntaxException, MLInputAdapterException, IOException, ClassNotFoundException {

        String[] modelStorage = modelStorageLocation.trim().split(":");
        String storageType = modelStorage[0];
        if(storageType.equals(REGISTRY_STORAGE_PREFIX)) {
            if(modelStorage[1].startsWith(PATH_TO_GOVERNANCE_REGISTRY)) {
                modelStorageLocation = modelStorage[1].substring(PATH_TO_GOVERNANCE_REGISTRY.length());
            } else {
                modelStorageLocation = modelStorage[1];
            }
        } else {
            storageType = MLConstants.DATASET_SOURCE_TYPE_FILE;
        }

        MLIOFactory ioFactory = new MLIOFactory(MLCoreServiceValueHolder.getInstance().getMlProperties());
        MLInputAdapter inputAdapter = ioFactory.getInputAdapter(storageType + MLConstants.IN_SUFFIX);
        InputStream in = inputAdapter.read(modelStorageLocation);
        ObjectInputStream ois = new ObjectInputStream(in);
        MLModel mlModel = (MLModel) ois.readObject();
        ois.close();
        return mlModel;

    }

    /**
     * Predict the value using the feature values
     * @param data  feature values array
     * @return      predicted value
     * @throws      MLModelBuilderException
     */
    public String predict(String[] data) throws MLModelHandlerException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        list.add(data);
        Predictor predictor = new Predictor(modelId, mlModel, list);
        List<?> predictions = predictor.predict();
        return predictions.get(0).toString();
    }

    /**
     * Return the map containing <feature-name, index> pairs
     * @return
     */
    public Map<String, Integer> getFeatures() {
        List<Feature> features = mlModel.getFeatures();
        Map<String, Integer> featureIndexMap = new HashMap<String, Integer>();
        for(Feature feature : features) {
            featureIndexMap.put(feature.getName(), feature.getIndex());
        }
        return featureIndexMap;
    }
    
    /**
     * Get new to old indices list of this model.
     * @return
     */
    public List<Integer> getNewToOldIndicesList() {
        return mlModel.getNewToOldIndicesList();
    }
}
