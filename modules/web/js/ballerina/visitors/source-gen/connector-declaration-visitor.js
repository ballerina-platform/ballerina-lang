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
import _ from 'lodash';
import log from 'log';
import EventChannel from 'event_channel';
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';

/**
 * @param parent
 * @constructor
 */
class ConnectorDeclarationVisitor extends AbstractSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitConnectorDeclaration(connectorDeclaration) {
        return true;
    }

    beginVisitConnectorDeclaration(connectorDeclaration) {
        /**
         * set the configuration start for the connector declaration
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        var connectorPkg = ((!_.isNil(connectorDeclaration.getConnectorPkgName()))
                             && (!_.isEqual(connectorDeclaration.getConnectorPkgName(), 'Current Package'))) ?
            (connectorDeclaration.getConnectorPkgName() + ":") : "";
        var constructedSource = connectorPkg +
            connectorDeclaration.getConnectorName() + ' ' + connectorDeclaration.getConnectorVariable() +
            ' = create ' + connectorPkg + connectorDeclaration.getConnectorName() +
            '(' + connectorDeclaration.getParams() + ')';
        this.appendSource(constructedSource);
        log.debug('Begin Visit Connector Declaration');
    }

    visitConnectorDeclaration(connectorDeclaration) {
        log.debug('Visit Connector Declaration');
    }

    endVisitConnectorDeclaration(connectorDeclaration) {
        this.appendSource(";\n");
        this.getParent().appendSource(this.getIndentation() + this.getGeneratedSource());
        log.debug('End Visit Connector Declaration');
    }
}

export default ConnectorDeclarationVisitor;
