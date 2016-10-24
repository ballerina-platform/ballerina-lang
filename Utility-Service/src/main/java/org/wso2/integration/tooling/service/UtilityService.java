/**
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

package org.wso2.integration.tooling.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the Microservice resource class.
 * See <a href="https://github.com/wso2/msf4j#getting-started">https://github.com/wso2/msf4j#getting-started</a>
 * for the usage of annotations.
 *
 * @since 1.0-SNAPSHOT
 */
@Path("/utility")
public class UtilityService {

    @POST
    @Path("/saveconfig")
    @Produces("text/plain")
    @Consumes("text/plain")
    public Response saveConfiguration(String payload) {

        System.out.println(payload);
        String location="" ;
        String config;
        Matcher locationMatcher = Pattern.compile("location=(.*?)&").matcher(payload);
        while (locationMatcher.find()) {
            location = locationMatcher.group(1);
        }
            config = payload.split("config=")[1];
        byte[] base64Config = Base64.getDecoder().decode(config);
        byte[] base64Location = Base64.getDecoder().decode(location);
        try {
            Files.write(Paths.get((new String(base64Location))), base64Config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.OK).entity("Success").type(MediaType.TEXT_PLAIN).build();
    }
}
