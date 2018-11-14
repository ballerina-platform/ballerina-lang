/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
import { createElement } from 'react';
import ReactDOM from 'react-dom';
import BallerinaDiagramWrapper from './BallerinaDiagramWrapper';
import '@ballerina/theme/default-theme/index.js/';
import './scss/themes/default.scss';
import '@ballerina/theme/font-ballerina';

/**
 * Renders Editable Ballerina Diagram on given HTMLElement.
 *
 * @param {HTMLElement} target Element to render on.
 * @param {string} docUri URI of the ballerina File. This will be used for subsequent API calls.
 * @param {number} width Preferred width in pixels.
 * @param {number} height Preferred Height in pixels.
 * @param {Function} getAST API to invoke to get AST given the URI.
 * @param {Function} onChange Will be invoked upon changes to Diagram. DocURI and new AST will be passed as args.
 * @param {Function} getEndpoints Will be invoked to get available endpoints.
 * @param {Function} parseFragment Will be invoked to get Ballerina Fragments parsed.
 * @param {Function} goToSource  Will be invoked to when a go-to-source button is cliked in Diagram.
 *                               Position info will be passed as args.
 *
 */
export default function renderEditableDiagram(target, docUri, width, height,
    getAST = () => Promise.resolve({}),
    onChange = () => {},
    getEndpoints,
    parseFragment,
    goToSource) {
    const props = {
        getAST,
        onChange,
        docUri,
        width,
        height,
        getEndpoints,
        parseFragment,
        goToSource,
    };
    target.classList.add('composer-library');
    const BalDiagramElement = createElement(BallerinaDiagramWrapper, props);
    ReactDOM.render(BalDiagramElement, target);
}
