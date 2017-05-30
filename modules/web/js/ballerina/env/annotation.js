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
import log from 'log';
import _ from 'lodash';
import EventChannel from 'event_channel';

/**
 * @class Annotation
 * @augments
 * @param {Object} args - data to create the Annotation
 * @param {string} args.name - name of annotation
 * @param {string} args.id - id of annotation
 * @constructor
 */
class Annotation extends EventChannel {
    constructor(args) {
        super(args);
        this._name = _.get(args, 'name', '');
        this._attachmentPoints = _.get(args, 'attachmentPoints', []);
        this._id = _.get(args, 'id', '');
    }

    /**
    * sets the name
    * @param {string} name
    */
    setName(name) {
        var oldName = this._name;
        this._name = name;
        this.trigger("name-modified", name, oldName);
    }

   /**
    * returns the name
    * @returns {string}
    */
    getName() {
        return this._name;
    }

   /**
    * sets the id
    * @param {string} id
    */
    setId(id) {
        this._id = id;
    }

   /**
    * returns the id
    * @returns {string}
    */
    getId() {
        return this._id;
    }

   /**
    * returns attachment points
    * @returns {string[]}
    */
    getAttachmentPoints(){
        return this._attachmentPoints;
    }

    addAttachmentPoints(attachmentPoints) {
        this._attachmentPoints.push(attachmentPoints);
    }

    /**
    * sets values from a json object
    * @param {Object} jsonNode
    */
    initFromJson(jsonNode) {
        var self = this;
        this.setName(jsonNode.name);

        _.each(jsonNode.attachmentPoints, function (attachmentPoint) {
            self.addAttachmentPoints(attachmentPoint);
        });

    }

}

export default Annotation;
