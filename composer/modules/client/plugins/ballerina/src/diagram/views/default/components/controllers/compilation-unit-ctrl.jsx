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
 */
import React from 'react';
import PropTypes from 'prop-types';
import { COMMANDS } from '../../../../../constants';
import Button from '../../../../../interactions/button';
import Area from '../../../../../interactions/area';
import Menu from '../../../../../interactions/menu';
import TopLevelElements from '../../../../../tool-palette/item-provider/compilation-unit-tools';
import Item from '../../../../../interactions/item';

/**
 * class to render Next statement.
 * @extends React.Component
 * @class CompilationUnitCtrl
 * */
class CompilationUnitCtrl extends React.Component {

    convertToAddItems(tools, node) {
        return tools.map((element, index) => {
            const data = {
                node,
                item: element,
            };

            if (element.seperator) {
                return <hr key={index} />;
            }
            return (
                <Item
                    key={element.name}
                    label={element.name}
                    icon={`fw fw-${element.icon}`}
                    callback={(menuItem) => {
                        const newNode = menuItem.item.nodeFactoryMethod();
                        menuItem.node.acceptDrop(newNode);
                    }}
                    data={data}
                />
            );
        });
    }

    /**
     * Render Function for the Next statement.
     * @return {React.Component} next node react component.
     * */
    render() {
        const node = this.props.model;
        const offsetX = node.getPackageDeclaration()
                || node.viewState.packageDefExpanded
                ? 320
                : 40;
        const x = this.context.config.panel.wrapper.gutter.h + offsetX;
        const y = this.context.config.panel.wrapper.gutter.v;
        const w = 50;
        const h = 50;
        if (node.isEmpty()) {
            // y = (node.viewState.bBox.h / 2);
        }
        const items = this.convertToAddItems(TopLevelElements, node);
        return [
            <Area bBox={{ x, y, w, h }} key='add-item'>
                <Button
                    buttonX={0}
                    buttonY={0}
                    showAlways
                    menuOverButton
                    buttonRadius={12}
                >
                    <Menu>
                        {items}
                    </Menu>
                </Button>
            </Area>,
        ];
    }

}

CompilationUnitCtrl.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

CompilationUnitCtrl.contextTypes = {
    config: PropTypes.instanceOf(Object).isRequired,
    command: PropTypes.shape({
        on: PropTypes.func,
        dispatch: PropTypes.func,
    }).isRequired,
    mode: PropTypes.string,
};

export default CompilationUnitCtrl;
