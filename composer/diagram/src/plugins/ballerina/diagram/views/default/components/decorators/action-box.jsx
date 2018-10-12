/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ImageUtil from '../../../../image-util';
import './action-box.css';
import { withDragStateKnowledge } from '../../../../../drag-drop/util';
import { Button } from 'semantic-ui-react';

/**
 * React component for Actionbox
 *
 * @class ActionBox
 * @extends {React.Component}
 */
class ActionBox extends React.Component {

    /**
     * Renders the view of action box.
     *
     * @returns {ReactElement} The view.
     *
     * @memberof ActionBox
     */
    render() {
        const iconSize = 25;
        const style = {
            position: 'absolute',
            ...this.props.style,
            left:  this.props.style.left - iconSize,
        };
        return (<div style={style}>
            <Button.Group
                className='lifeline icons'
            >
                {!this.props.disableButtons.delete &&
                    <Button
                        onClick={this.props.onDelete}
                    >
                        <i className='fw'>{ImageUtil.getCodePoint('delete')}</i>
                    </Button>
                }
                {!this.props.disableButtons.jump &&
                    <Button
                        onClick={this.props.onJumptoCodeLine}
                    >
                        <i className='fw'>{ImageUtil.getCodePoint('code-view')}</i>
                    </Button>
                }
            </Button.Group>
        </div>);
    }

}

ActionBox.propTypes = {
    onDelete: PropTypes.func.isRequired,
    onJumptoCodeLine: PropTypes.func.isRequired,
    disableButtons: PropTypes.shape({
        debug: PropTypes.bool,
        delete: PropTypes.bool,
        jump: PropTypes.bool,
    }).isRequired,
};

ActionBox.defaultProps = {
    disableButtons: {
        debug: false,
        delete: false,
        jump: false,
    },
    style: {},
};


export default withDragStateKnowledge(ActionBox);
