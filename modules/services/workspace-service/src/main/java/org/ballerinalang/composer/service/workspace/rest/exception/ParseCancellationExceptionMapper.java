/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.composer.service.workspace.rest.exception;

import com.google.gson.JsonObject;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper class for semantic exceptions.
 */
public class ParseCancellationExceptionMapper implements ExceptionMapper<ParseCancellationException> {
    private static final Logger logger = LoggerFactory.getLogger(ParseCancellationExceptionMapper.class);

    @Override
    public Response toResponse(ParseCancellationException exception) {
        logger.error("error: " + exception.getMessage(), exception);
        JsonObject entity = new JsonObject();
        entity.addProperty("errorMessage", exception.getMessage());
        return Response.status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", '*')
                .entity(entity)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
