/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.extensions.ballerina.symbol;

import org.ballerinalang.langserver.LSGlobalContext;
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.index.LSIndexImpl;
import org.ballerinalang.langserver.index.dao.BObjectTypeSymbolDAO;
import org.ballerinalang.langserver.index.dao.BPackageSymbolDAO;
import org.ballerinalang.langserver.index.dao.DAOType;
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Ballerina Symbol Service LS Extension.
 *
 * @since 0.981.2
 */
public class BallerinaSymbolServiceImpl implements BallerinaSymbolService {

    private static final Logger logger = LoggerFactory.getLogger(BallerinaSymbolServiceImpl.class);
    private final LSIndexImpl lsIndex;

    public BallerinaSymbolServiceImpl(LSGlobalContext lsGlobalContext) {
        this.lsIndex = lsGlobalContext.get(LSGlobalContextKeys.LS_INDEX_KEY);
    }

    @Override
    public CompletableFuture<BallerinaEndpointsResponse> endpoints() {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaEndpointsResponse response = new BallerinaEndpointsResponse();
            response.setEndpoints(getEndpoints());
            return response;
        });
    }

    private List<Endpoint> getEndpoints() {
        final List<Endpoint> endpoints = new ArrayList<>();
        try {
            BPackageSymbolDAO pkgDAO = ((BPackageSymbolDAO) lsIndex.getDaoFactory().get(DAOType.PACKAGE_SYMBOL));
            BObjectTypeSymbolDAO objDAO = ((BObjectTypeSymbolDAO) lsIndex.getDaoFactory().get(DAOType.OBJECT_TYPE));

            List<BPackageSymbolDTO> pkgDTOs = pkgDAO.getAll();
            List<BObjectTypeSymbolDTO> allEndpoints = objDAO.getAllEndpoints();

            pkgDTOs.forEach(pkgDTO -> {
                List<Endpoint> endpointsList = allEndpoints.stream()
                        .filter(endpoint -> endpoint.getPackageId() == pkgDTO.getId())
                        .map(objDTO -> new Endpoint(objDTO.getName(), pkgDTO.getName(), pkgDTO.getOrgName()))
                        .collect(Collectors.toList());
                endpoints.addAll(endpointsList);
            });
        } catch (Exception e) {
            // Above catch is to fail safe composer front end due to core errors.
            logger.warn("Error while loading package: " + e.getMessage());
        }
        return endpoints;
    }

}
