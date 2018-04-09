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

package org.ballerinalang.ballerina.swagger.convertor.service;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Ballerina conversion to swagger and open API will test in this class.
 */

public class SwaggerConverterUtilsTest {
    //Sample Ballerina Service definitions to be used for tests.
    private static String sampleBallerinaServiceString = "\n" +
            "import ballerina/http;\n" +
            "import ballerina/http.swagger;\n" +
            "\n" +
            "\n" +
            "endpoint http:ServiceEndpoint ep0 {\n" +
            "    host: \"localhost\",\n" +
            "    port: 9091\n" +
            "};\n" +
            "\n" +
            "@swagger:ServiceInfo { \n" +
            "    title: \"Swagger Petstore\",\n" +
            "    description: \"This is a sample server Petstore server.  You can find out more about Swagger at "
            + "[http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  "
            + "For this sample, you can use the api key &#x60;special-key&#x60; to test the authorization "
            + "filters.\",\n" +
            "    serviceVersion: \"1.0.0\",\n" +
            "    termsOfService: \"http://swagger.io/terms/\",\n" +
            "    contact: {name: \"\", email: \"apiteam@swagger.io\", url: \"\"},\n" +
            "    license: {name: \"Apache 2.0\", url: \"http://www.apache.org/licenses/LICENSE-2.0.html\"},\n" +
            "    tags: [\n" +
            "        {name: \"pet\", description: \"Everything about your Pets\", externalDocs: { description: "
            + "\"Find out more\", url: \"http://swagger.io\" } },\n" +
            "        {name: \"store\", description: \"Access to Petstore orders\", externalDocs: {  } },\n" +
            "        {name: \"user\", description: \"Operations about user\", externalDocs: { description: "
            + "\"Find out more about our store\", url: \"http://swagger.io\" } }\n" +
            "    ],\n" +
            "    externalDocs: { description: \"Find out more about Swagger\", url: \"http://swagger.io\" },\n" +
            "    security: [\n" +
            "    ]\n" +
            "}\n" +
            "@http:ServiceConfig {\n" +
            "    basePath: \"/v2\"\n" +
            "}\n" +
            "service<http:Service> SwaggerPetstore bind ep0 {\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"pet\"],\n" +
            "        summary: \"Add a new pet to the store\",\n" +
            "        description: \"\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"POST\"],\n" +
            "        path:\"/pet\"\n" +
            "    }\n" +
            "    addPet (endpoint outboundEp, http:Request req) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample addPet Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"pet\"],\n" +
            "        summary: \"Update an existing pet\",\n" +
            "        description: \"\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"PUT\"],\n" +
            "        path:\"/pet\"\n" +
            "    }\n" +
            "    updatePet (endpoint outboundEp, http:Request req) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample updatePet Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"pet\"],\n" +
            "        summary: \"Finds Pets by status\",\n" +
            "        description: \"Multiple status values can be provided with comma separated strings\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "            {\n" +
            "                name: \"status\",\n" +
            "                inInfo: \"query\",\n" +
            "                description: \"Status values that need to be considered for filter\", \n" +
            "                required: true, \n" +
            "                allowEmptyValue: \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"GET\"],\n" +
            "        path:\"/pet/findByStatus\"\n" +
            "    }\n" +
            "    findPetsByStatus (endpoint outboundEp, http:Request req) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample findPetsByStatus Response sanjeewa\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"pet\"],\n" +
            "        summary: \"Finds Pets by tags\",\n" +
            "        description: \"Muliple tags can be provided with comma separated strings. Use tag1, tag2, tag3 "
            + "for testing.\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "            {\n" +
            "                name: \"tags\",\n" +
            "                inInfo: \"query\",\n" +
            "                description: \"Tags to filter by\", \n" +
            "                required: true, \n" +
            "                allowEmptyValue: \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"GET\"],\n" +
            "        path:\"/pet/findByTags\"\n" +
            "    }\n" +
            "    deprecated {}\n" +
            "    findPetsByTags (endpoint outboundEp, http:Request req) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample findPetsByTags Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"pet\"],\n" +
            "        summary: \"Find pet by ID\",\n" +
            "        description: \"Returns a single pet\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "            {\n" +
            "                name: \"petId\",\n" +
            "                inInfo: \"path\",\n" +
            "                description: \"ID of pet to return\", \n" +
            "                required: true, \n" +
            "                allowEmptyValue: \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"GET\"],\n" +
            "        path:\"/pet/{petId}\"\n" +
            "    }\n" +
            "    getPetById (endpoint outboundEp, http:Request req, string petId) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample getPetById Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"pet\"],\n" +
            "        summary: \"Updates a pet in the store with form data\",\n" +
            "        description: \"\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "            {\n" +
            "                name: \"petId\",\n" +
            "                inInfo: \"path\",\n" +
            "                description: \"ID of pet that needs to be updated\", \n" +
            "                required: true, \n" +
            "                allowEmptyValue: \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"POST\"],\n" +
            "        path:\"/pet/{petId}\"\n" +
            "    }\n" +
            "    updatePetWithForm (endpoint outboundEp, http:Request req, string petId) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample updatePetWithForm Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"pet\"],\n" +
            "        summary: \"Deletes a pet\",\n" +
            "        description: \"\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "            {\n" +
            "                name: \"api_key\",\n" +
            "                inInfo: \"header\",\n" +
            "                description: \"\",  \n" +
            "                allowEmptyValue: \"\"\n" +
            "            },\n" +
            "            {\n" +
            "                name: \"petId\",\n" +
            "                inInfo: \"path\",\n" +
            "                description: \"Pet id to delete\", \n" +
            "                required: true, \n" +
            "                allowEmptyValue: \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"DELETE\"],\n" +
            "        path:\"/pet/{petId}\"\n" +
            "    }\n" +
            "    deletePet (endpoint outboundEp, http:Request req, string petId) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample deletePet Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"pet\"],\n" +
            "        summary: \"uploads an image\",\n" +
            "        description: \"\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "            {\n" +
            "                name: \"petId\",\n" +
            "                inInfo: \"path\",\n" +
            "                description: \"ID of pet to update\", \n" +
            "                required: true, \n" +
            "                allowEmptyValue: \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"POST\"],\n" +
            "        path:\"/pet/{petId}/uploadImage\"\n" +
            "    }\n" +
            "    uploadFile (endpoint outboundEp, http:Request req, string petId) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample uploadFile Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"store\"],\n" +
            "        summary: \"Returns pet inventories by status\",\n" +
            "        description: \"Returns a map of status codes to quantities\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"GET\"],\n" +
            "        path:\"/store/inventory\"\n" +
            "    }\n" +
            "    getInventory (endpoint outboundEp, http:Request req) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample getInventory Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"store\"],\n" +
            "        summary: \"Place an order for a pet\",\n" +
            "        description: \"\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"POST\"],\n" +
            "        path:\"/store/order\"\n" +
            "    }\n" +
            "    placeOrder (endpoint outboundEp, http:Request req) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample placeOrder Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"store\"],\n" +
            "        summary: \"Find purchase order by ID\",\n" +
            "        description: \"For valid response try integer IDs with value &gt;&#x3D; 1 and &lt;&#x3D; 10. "
            + "Other values will generated exceptions\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "            {\n" +
            "                name: \"orderId\",\n" +
            "                inInfo: \"path\",\n" +
            "                description: \"ID of pet that needs to be fetched\", \n" +
            "                required: true, \n" +
            "                allowEmptyValue: \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"GET\"],\n" +
            "        path:\"/store/order/{orderId}\"\n" +
            "    }\n" +
            "    getOrderById (endpoint outboundEp, http:Request req, string orderId) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample getOrderById Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"store\"],\n" +
            "        summary: \"Delete purchase order by ID\",\n" +
            "        description: \"For valid response try integer IDs with positive integer value. Negative or "
            + "non-integer values will generate API errors\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "            {\n" +
            "                name: \"orderId\",\n" +
            "                inInfo: \"path\",\n" +
            "                description: \"ID of the order that needs to be deleted\", \n" +
            "                required: true, \n" +
            "                allowEmptyValue: \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"DELETE\"],\n" +
            "        path:\"/store/order/{orderId}\"\n" +
            "    }\n" +
            "    deleteOrder (endpoint outboundEp, http:Request req, string orderId) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample deleteOrder Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"user\"],\n" +
            "        summary: \"Create user\",\n" +
            "        description: \"This can only be done by the logged in user.\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"POST\"],\n" +
            "        path:\"/user\"\n" +
            "    }\n" +
            "    createUser (endpoint outboundEp, http:Request req) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample createUser Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"user\"],\n" +
            "        summary: \"Creates list of users with given input array\",\n" +
            "        description: \"\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"POST\"],\n" +
            "        path:\"/user/createWithArray\"\n" +
            "    }\n" +
            "    createUsersWithArrayInput (endpoint outboundEp, http:Request req) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample createUsersWithArrayInput Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"user\"],\n" +
            "        summary: \"Creates list of users with given input array\",\n" +
            "        description: \"\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"POST\"],\n" +
            "        path:\"/user/createWithList\"\n" +
            "    }\n" +
            "    createUsersWithListInput (endpoint outboundEp, http:Request req) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample createUsersWithListInput Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"user\"],\n" +
            "        summary: \"Logs user into the system\",\n" +
            "        description: \"\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "            {\n" +
            "                name: \"username\",\n" +
            "                inInfo: \"query\",\n" +
            "                description: \"The user name for login\", \n" +
            "                required: true, \n" +
            "                allowEmptyValue: \"\"\n" +
            "            },\n" +
            "            {\n" +
            "                name: \"password\",\n" +
            "                inInfo: \"query\",\n" +
            "                description: \"The password for login in clear text\", \n" +
            "                required: true, \n" +
            "                allowEmptyValue: \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"GET\"],\n" +
            "        path:\"/user/login\"\n" +
            "    }\n" +
            "    loginUser (endpoint outboundEp, http:Request req) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample loginUser Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"user\"],\n" +
            "        summary: \"Logs out current logged in user session\",\n" +
            "        description: \"\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"GET\"],\n" +
            "        path:\"/user/logout\"\n" +
            "    }\n" +
            "    logoutUser (endpoint outboundEp, http:Request req) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample logoutUser Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"user\"],\n" +
            "        summary: \"Get user by user name\",\n" +
            "        description: \"\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "            {\n" +
            "                name: \"username\",\n" +
            "                inInfo: \"path\",\n" +
            "                description: \"The name that needs to be fetched. Use user1 for testing. \", \n" +
            "                required: true, \n" +
            "                allowEmptyValue: \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"GET\"],\n" +
            "        path:\"/user/{username}\"\n" +
            "    }\n" +
            "    getUserByName (endpoint outboundEp, http:Request req, string username) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample getUserByName Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"user\"],\n" +
            "        summary: \"Updated user\",\n" +
            "        description: \"This can only be done by the logged in user.\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "            {\n" +
            "                name: \"username\",\n" +
            "                inInfo: \"path\",\n" +
            "                description: \"name that need to be updated\", \n" +
            "                required: true, \n" +
            "                allowEmptyValue: \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"PUT\"],\n" +
            "        path:\"/user/{username}\"\n" +
            "    }\n" +
            "    updateUser (endpoint outboundEp, http:Request req, string username) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample updateUser Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "    @swagger:ResourceInfo {\n" +
            "        tags: [\"user\"],\n" +
            "        summary: \"Delete user\",\n" +
            "        description: \"This can only be done by the logged in user.\",\n" +
            "        externalDocs: {  },\n" +
            "        parameters: [\n" +
            "            {\n" +
            "                name: \"username\",\n" +
            "                inInfo: \"path\",\n" +
            "                description: \"The name that needs to be deleted\", \n" +
            "                required: true, \n" +
            "                allowEmptyValue: \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "    @http:ResourceConfig {\n" +
            "        methods:[\"DELETE\"],\n" +
            "        path:\"/user/{username}\"\n" +
            "    }\n" +
            "    deleteUser (endpoint outboundEp, http:Request req, string username) {\n" +
            "        //stub code - fill as necessary\n" +
            "        http:Response resp = {};\n" +
            "        string payload = \"Sample deleteUser Response\";\n" +
            "        resp.setStringPayload(payload);\n" +
            "        _ = outboundEp -> respond(resp);\n" +
            "    }\n" +
            "\n" +
            "}\n";

    private static String complexServiceSample = "import ballerina/http;\n" +
            "import ballerina.runtime;service<http> failoverService {\n" +
            "    int[] failoverHttpStatusCodes = [400, 404, 500];\n" +
            "    resiliency:FailoverConfig errorCode = {failoverCodes:failoverHttpStatusCodes};\n" +
            "    @http:resourceConfig {\n" +
            "        path:\"/\"\n" +
            "    }    resource failoverPostResource (http:Connection conn, http:Request req) {\n" +
            "        endpoint<http:HttpClient> endPoint {\n" +
            "            create resiliency:Failover(\n" +
            "                    [create http:HttpClient(\"http://localhost:23456/mock\", {}),\n" +
            "                     create http:HttpClient(\"http://localhost:9090/echo\",\n" +
            "                                    {timeoutMillies:5000}),\n" +
            "                     create http:HttpClient(\"http://localhost:9090/mock\", {})],\n" +
            "                     errorCode);\n" +
            "        }        http:Response inResponse = {};\n" +
            "        http:HttpConnectorError err;        http:Request outRequest = {};\n" +
            "        json requestPayload = {\"name\":\"Ballerina\"};\n" +
            "        outRequest.setJsonPayload(requestPayload);\n" +
            "        inResponse, err = endPoint.post(\"/\", outRequest);        " +
            "http:Response outResponse = {};\n" +
            "        if (err != null) {\n" +
            "            outResponse.statusCode = err.statusCode;\n" +
            "            outResponse.setStringPayload(err.message);\n" +
            "            _ = conn.respond(outResponse);\n" +
            "        } else {\n" +
            "            _ = conn.forward(inResponse);\n" +
            "        }\n" +
            "    }}\n" +
            "service<http> echo {\n" +
            "    @http:resourceConfig {\n" +
            "        methods:[\"POST\", \"PUT\", \"GET\"],\n" +
            "        path:\"/\"\n" +
            "    }\n" +
            "    resource echoResource (http:Connection conn, http:Request req) {\n" +
            "        http:Response outResponse = {};\n" +
            "        runtime:sleepCurrentWorker(30000);\n" +
            "        outResponse.setStringPayload(\"Resource is invoked\");\n" +
            "        _ = conn.respond(outResponse);\n" +
            "    }\n" +
            "}service<http> mock {\n" +
            "    @http:resourceConfig {\n" +
            "        methods:[\"POST\", \"PUT\", \"GET\"],\n" +
            "        path:\"/\"\n" +
            "    }\n" +
            "    resource mockResource (http:Connection conn, http:Request req) {\n" +
            "        http:Response outResponse = {};\n" +
            "        outResponse.setStringPayload(\"Mock Resource is Invoked.\");\n" +
            "        _ = conn.respond(outResponse);\n" +
            "    }\n" +
            "}";

    @Test(description = "Test ballerina to swagger conversion")
    public void testBallerinaToSwaggerConversion() {
        String serviceName = "nyseStockQuote";
        try {
            String swaggerDefinition = SwaggerConverterUtils.generateSwaggerDefinitions(sampleBallerinaServiceString,
                    serviceName);
            Assert.assertNotNull(swaggerDefinition);
        } catch (IOException e) {
            Assert.fail("Error while converting ballerina service to swagger definition");
        }
    }


    @Test(description = "Test OAS definition generation from ballerina service")
    public void testOASGeneration() {
        String serviceName = "nyseStockQuote";
        try {
            String swaggerDefinition = SwaggerConverterUtils.generateOAS3Definitions(sampleBallerinaServiceString,
                    serviceName);
            Assert.assertNotNull(swaggerDefinition);
        } catch (IOException e) {
            Assert.fail("Error while converting ballerina service to swagger definition");
        }
    }


    @Test(description = "Test OAS definition generation from ballerina service with empty service name")
    public void testOASGenerationWithEmptyServiceName() {
        String serviceName = "";
        try {
            String swaggerDefinition = SwaggerConverterUtils.generateOAS3Definitions(sampleBallerinaServiceString,
                    serviceName);
            Assert.assertNotNull(swaggerDefinition);
        } catch (IOException e) {
            Assert.fail("Error while converting ballerina service to swagger definition with empty service name");
        }
    }


    @Test(description = "Test OAS definition generation from ballerina service with complex service")
    public void testOASGenerationWithEmptyServiceNameM() {
        String serviceName = "mock";
        try {
            String swaggerDefinition = SwaggerConverterUtils.generateOAS3Definitions(complexServiceSample,
                    serviceName);
            Assert.assertNotNull(swaggerDefinition);
        } catch (IOException e) {
            Assert.fail("Error while converting ballerina service to swagger definition with empty service name");
        }
    }

}
