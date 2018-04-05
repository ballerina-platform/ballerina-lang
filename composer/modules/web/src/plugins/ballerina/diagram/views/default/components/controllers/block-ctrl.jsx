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
import WorkerTools from '../../../../../tool-palette/item-provider/worker-tools';
import ControllerUtil from './controller-util';
import TreeUtil from './../../../../../model/tree-util';
import WorkerButton from '../../../../../interactions/worker-button';

/**
 * class to render Next statement.
 * @extends React.Component
 * @class BlockCtrl
 * */
class BlockCtrl extends React.Component {

    /**
     * Render Function for the Next statement.
     * @return {React.Component} next node react component.
     * */
    render() {
        const node = this.props.model;
        const bBox = Object.assign({}, node.viewState.bBox);

        const button = {};
        button.x = 8;
        button.y = bBox.h - 20;

        bBox.x -= this.context.config.statement.height;
        bBox.h += this.context.config.statement.height;

        let showAlways = false;
        // see if the parent node is collapsed.
        let parentNode = node;
        while (parentNode.parent) {
            parentNode = parentNode.parent;
            if (
                TreeUtil.isFunction(parentNode) ||
                TreeUtil.isResource(parentNode) ||
                TreeUtil.isAction(parentNode)
            ) {
                break;
            }
        }
        if (parentNode.viewState.collapsed || parentNode.lambda) {
            return null;
        }

        // Following logic will skip button rendering for blocks which are not on lines.
        if (!TreeUtil.isLineBlock(node)) {
            return [];
        }
        // change + position based on node type.
        if (TreeUtil.isWhile(node.parent)) {
            // bBox.h -= this.context.config.statement.height * 1.5;
        } else if (TreeUtil.isIf(node.parent)) {
            // bBox.h -= this.context.config.statement.height * 1.5;
            if (TreeUtil.isElseBlock(node) && TreeUtil.isEmptyBlock(node)) {
                bBox.y -= this.context.config.statement.height * 2;
            }
        } else if (TreeUtil.isTry(node.parent)) {
            // do nothing
        } else if (TreeUtil.isMatchPatternClause(node.parent)) {
            // do nothing
        } else if (TreeUtil.isCatch(node.parent)) {
            // do nothing
        } else if (TreeUtil.isFinally(node)) {
            // do nothing
        } else if (TreeUtil.isForeach(node.parent)) {
            // do nothing
        } else if (TreeUtil.isTransaction(node.parent)) {
            // do nothing
            button.y = 0;
        } else if (TreeUtil.isForkJoin(node.parent)) {
            // do nothing
        } else {
            bBox.y += bBox.h - (this.context.config.statement.height * 0.5);
            bBox.h = this.context.config.statement.height * 1.5;
            button.y = 0;
            showAlways = true;
        }
        // positioning changes to + button.

        const items = ControllerUtil.convertToAddItems(WorkerTools, node);
        // Not implemented.
        return (
            <WorkerButton
                bBox={bBox}
                model={node}
                items={items}
                button={button}
                showAlways={showAlways}
            />
        );
    }
}

BlockCtrl.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

BlockCtrl.contextTypes = {
    config: PropTypes.instanceOf(Object).isRequired,
    command: PropTypes.shape({
        on: PropTypes.func,
        dispatch: PropTypes.func,
    }).isRequired,
    mode: PropTypes.string,
};

export default BlockCtrl;
