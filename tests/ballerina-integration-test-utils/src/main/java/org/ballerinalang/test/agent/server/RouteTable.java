/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.agent.server;

import io.netty.handler.codec.http.HttpMethod;

import java.util.ArrayList;

/**
 * The RouteTable class contains all URL routes in the WebServer.
 */
class RouteTable {
    private final ArrayList<Route> routes;

    RouteTable() {
        this.routes = new ArrayList<Route>();
    }

    void addRoute(final Route route) {
        this.routes.add(route);
    }

    Route findRoute(final HttpMethod method, final String path) {
        for (final Route route : routes) {
            if (route.matches(method, path)) {
                return route;
            }
        }

        return null;
    }
}
