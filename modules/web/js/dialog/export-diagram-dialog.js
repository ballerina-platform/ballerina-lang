/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import $ from 'jquery';
import log from 'log';
import Backbone from 'backbone';
import FileBrowser from 'file_browser';
import 'bootstrap';
import './dialog.css';

/**
 * Class for export diagram diagram.
 *
 * @class ExportDiagramDialog
 * */
const ExportDiagramDialog = Backbone.View.extend({
    /**
     * @augments Backbone.View
     * @constructs
     * @class SaveToFileDialog
     * @param {Object} options configuration options for the SaveToFileDialog
     */
    initialize(options) {
        this.css = '.svg-container{background:none;font-family:"Roboto",Arial,Helvetica,sans-serif;font-size:14px}' +
            '.panel-header-controls{display:none}.parameter-delete-icon-wrapper{display:none}.property-pane-action' +
            '-button-delete{display:none}.parameter-delete-icon{display:none}.svg-container > .drop-zone{fill:gree' +
            'n;fill-opacity:0}.import-badge-text{fill:#fff}.package-definition-head text{dominant-baseline:central' +
            '}.text-placeholder{fill:#d8d8d8;shape-rendering:crispEdges;stroke:#9e9c9c;stroke-width:1;stroke-dasha' +
            'rray:3,3}.tag-component-label{fill:#999}.parameter-space{display:none}.hide-action{display:none}.conn' +
            'ector-activation-container .activation-zone.active{fill-opacity:.2}.connector-activation-container .a' +
            'ctivation-zone.active.block{fill:red}.connector-activation-container .activation-zone{fill:green;fill' +
            '-opacity:0}.parameter-separator{stroke:#ddd;stroke-width:2;cursor:pointer}.life-line-hider{stroke:#ff' +
            'f;stroke-width:3}.attribute-content-operations-wrapper{fill:#615c5c;shape-rendering:crispEdges;height' +
            ':25px}.delete-button-icon{display:none;opacity:.5;cursor:pointer;height:15px}.delete-button-icon:hove' +
            'r{fill:#912d31;opacity:.9;cursor:pointer}.annotation-input{fill:#eee;stroke:#b5b5b5;stroke-width:1;st' +
            'roke-dasharray:3,3}.annotation-input-placeholder{fill:#333}.annotation-delete-wrapper{display:none;fi' +
            'll:#dfdfdf;stroke:#dfdfdf;stroke-width:1}.annotation-attribute-wrapper{fill:#f3f3f3;shape-rendering:c' +
            'rispEdges;stroke:#ccc;stroke-width:.5}.annotation-attribute-wrapper-text{fill:#333}.panel-label{left:' +
            '10px;fill:#252525}.annotation-input-placeholder{fill:#999}#alerts-container{display:none}.right-conta' +
            'iner{background-color:#232323}#main-navbar{background-color:#232323;border-top:solid #1e1e1e;border-b' +
            'ottom:solid #1e1e1e}.header.header-default{background-color:#16191d;min-height:27px}#editor-container' +
            '{background-color:#fff}.editor-scroller{cursor:move}.circle-hide{display:none}.option-menu-hide{displ' +
            'ay:none}svg text::selection{background:none}.non-user-selectable{-webkit-touch-callout:none;-webkit-u' +
            'ser-select:none;-khtml-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none}.' +
            'canvas-container{width:100%}.canvas-heading{cursor:pointer;border-left:3px solid #3498db}#tree-contai' +
            'ner{background-color:#272727;border:solid #373737}#tree-action-bar{background-color:#373737;color:#ed' +
            'eded;border-bottom:solid #1e1e1e}.tree-view{color:#7e7e7e;font-size:1.2em}.tree-view li > .icon:befor' +
            'e{color:#272727}.tree-view li > .icon{background:#fdfdfd}.tree-action{float:right}i.tree-action{paddi' +
            'ng:15px 8px}span.tree-action{padding:4px}.tree-view input{height:32px;color:#000}header .brand img.lo' +
            'go{height:15px;padding:0 10px}header .brand{padding:0 10px;margin-right:25px;line-height:35px}header ' +
            '.brand h1{font-size:15px}ol#breadcrumbList{background-color:#444}.file-dropdown-menu{background:#1e1e' +
            '1e;top:50%;font-size:11px}.settings-icon{padding-top:5px}.source-icon{padding-top:5px}.file-browser-c' +
            'ontainer{padding:15px;max-height:450px;overflow-x:hidden;overflow-y:scroll}header .brand img.logo{hei' +
            'ght:17px;padding:0 7px}header .brand h1{font-size:14px;font-weight:200;color:#d6d6d6}ol#breadcrumbLis' +
            't{background-color:#131313}#breadcrumb-container{background-color:#282b2f;height:28px;overflow:hidden' +
            ';line-height:15px}#icon-toggle-source{height:100%;width:100%;object-fit:contain}#icon-toggle-design{h' +
            'eight:100%;width:100%;object-fit:contain}.statement-rect{fill:#fff;stroke:#444;stroke-width:1}.statem' +
            'ent-rect-error{fill:#fff;stroke:#ff0202;stroke-width:1}.background-empty-rect{fill:#fff;fill-opacity:' +
            '0;stroke:#000;stroke-width:1}.background-empty-rect-error{fill:#fff;fill-opacity:0;stroke:#ff0202;str' +
            'oke-width:1}.statement-text{text-anchor:middle;dominant-baseline:central;width:120px;max-width:250px}' +
            '.panel-label{dominant-baseline:middle}.condition-text{text-anchor:start;dominant-baseline:central;wid' +
            'th:120px;max-width:250px;fill:#000}.service-annotation-button{float:right;position:relative}.service-' +
            'variable-button{float:right;position:relative;display:block}.service-variable-typebutton{float:right}' +
            '.variable-pane{background-color:#fff;position:absolute;top:9px;left:28px;margin:auto;min-height:28px}' +
            '.resource-variable-pane{background-color:#f3f3f3;padding:7px 7px 0;height:38px;position:absolute;disp' +
            'lay:none;margin:auto}.resource-variable-pane input{padding-left:5px;height:25px}.resource-variable-pa' +
            'ne select{height:25px;margin-right:5px}.resource-variable-pane form{width:326px}.resource-variable-pa' +
            'ne input{padding-left:5px;height:25px}.resource-variable-pane form > input{margin-right:5px}button.va' +
            'riable-list{font-size:small;margin-right:2px;height:20px}.variable-wrapper i,.attachment-wrapper i{pa' +
            'dding:6px;font-size:8px;background-color:#fff;color:#19b89c;opacity:.5}.attachment-wrapper i{color:#0' +
            '00005}.variable-wrapper i:hover{cursor:pointer;color:#000}.variable-type{float:left;color:#fff;paddin' +
            'g:2px 8px}.variable-identifier{float:left;padding:2px 8px}.variable-wrapper-message i{border:0}.varia' +
            'ble-type-message{background-color:#19b89c}.variable-identifier-message{border:0}.variable-wrapper-con' +
            'nection i{border:1px solid orange}.variable-type-connection{background-color:orange}.variable-identif' +
            'ier-connection{border:1px solid orange;border-right-width:0}.variable-wrapper-string i{border:1px sol' +
            'id purple;border-left-width:0}.variable-type-string{background-color:purple}.variable-identifier-stri' +
            'ng{border:1px solid purple;border-right-width:0}.variable-wrapper-int i{border:1px solid #483d8b;bord' +
            'er-left-width:0}.variable-type-int{background-color:#483d8b}.variable-identifier-int{border:1px solid' +
            ' #483d8b;border-right-width:0}.variable-wrapper-exception i{border:1px solid #8b0000;border-left-widt' +
            'h:0}.variable-type-exception{background-color:#8b0000}.variable-identifier-exception{border:1px solid' +
            ' #8b0000;border-right-width:0}.variable-wrapper-boolean i{border:1px solid #156b8b;border-left-width:' +
            '0}.variable-type-boolean{background-color:#156b8b}.variable-identifier-boolean{border:1px solid #156b' +
            '8b;border-right-width:0}.variable-wrapper-double i{border:1px solid #063B49;border-left-width:0}.vari' +
            'able-type-double{background-color:#063B49}.variable-identifier-double{border:1px solid #063B49;border' +
            '-right-width:0}.variable-wrapper-float i{border:1px solid #009CCC;border-left-width:0}.variable-type-' +
            'float{background-color:#009CCC}.variable-identifier-float{border:1px solid #009CCC;border-right-width' +
            ':0}.variable-wrapper-long i{border:1px solid #4864aa;border-left-width:0}.variable-type-long{backgrou' +
            'nd-color:#4864aa}.variable-identifier-long{border:1px solid #4864aa;border-right-width:0}.variable-wr' +
            'apper-json i{border:1px solid #004d52;border-left-width:0}.variable-type-json{background-color:#004d5' +
            '2}.variable-identifier-json{border:1px solid #004d52;border-right-width:0}.variable-wrapper-xml i{bor' +
            'der:1px solid #232323;border-left-width:0}.variable-type-xml{background-color:#232323}.variable-ident' +
            'ifier-xml{border:1px solid #232323;border-right-width:0}.property-panel-svg{width:100%;min-height:500' +
            'px;background:none}.property-pane-action-button-wrapper{stroke:#000;stroke-width:.5;fill:#FFF}.statem' +
            'ent-action-button-wrapper{stroke:#000;stroke-width:.5;fill:#FFF}.property-pane-form-wrapper{width:265' +
            'px;max-height:250px;border:1px solid #cdcdcd;border-top:2px solid #7b7b7b;background:#FFF;position:ab' +
            'solute}.expression-editor-form-wrapper{width:265px;max-height:250px;background:#FFF;position:absolute' +
            '}.property-pane-form-heading{height:20px;border-bottom:1px solid #cdcdcd;padding-bottom:23px}.propert' +
            'y-pane-form-heading-icon{padding-left:10px;padding-top:5px;padding-bottom:5px}.property-pane-form-hea' +
            'ding-text{padding-left:7px}.property-pane-form-heading-close-icon{float:right;padding-right:10px;padd' +
            'ing-top:5px;padding-bottom:5px;opacity:.3;cursor:pointer}.property-pane-form-heading-close-icon:hover' +
            '{opacity:1}.property-pane-form-body-property-wrapper{padding:7px 7px 2px}.property-pane-form-body-pro' +
            'perty-wrapper input{width:100%}.expression-editor-form-body-property-wrapper input{width:100%;height:' +
            '100%;min-width:100%;border:1px solid;text-align:left;background:#333;color:#fff;opacity:.8}.property-' +
            'pane-form-body-add-button{width:100%}.service-annotation-wrapper{position:absolute}.service-annotatio' +
            'n-header-wrapper{margin:3px 10px 0}.service-annotation-header-wrapper input[type=text]{height:25px;ma' +
            'rgin-top:2px;border:1px solid #ccc;width:50%;margin-left:10px;padding-left:5px}.service-annotation-he' +
            'ader-wrapper span{margin-left:10px;height:25px;width:25px;overflow:hidden;margin-top:2px}.top-right-c' +
            'ontrols-container-editor-pane{display:none}.controls-container-font-action{font-size:15px;line-height' +
            ':1.6}.package-btn{position:absolute;right:0;top:6px;z-index:30}.variable-btn{position:absolute;z-inde' +
            'x:30;opacity:.4}.variable-btn:hover{opacity:1;cursor:pointer}.right-icon-clickable{padding:5px 7px}.c' +
            'ollapser:hover{color:#333}.main-action-wrapper{position:absolute;right:17px;width:400px;overflow:hidd' +
            'en;z-index:20;top:0}.action-icon-wrapper{position:absolute;top:8px;right:28px;height:25px;width:25px;' +
            'overflow:hidden;font-size:9px;cursor:pointer;opacity:1}.action-icon-wrapper:hover{opacity:1}.action-c' +
            'ontent-wrapper{background-color:#eaeaea;border:1px solid #f0f0f0}.action-content-wrapper-body{backgro' +
            'und-color:#fff;padding:10px}.action-content-wrapper-heading{height:37px;padding-left:10px;padding-top' +
            ':6px}.action-content-wrapper-heading input[type=text]{padding-left:7px;margin-left:2px;width:276px;bo' +
            'rder:1px solid #ccc}.package-name-wrapper span{font-weight:700}.package-name-wrapper input[type=text]' +
            '{padding-left:7px;margin-left:2px;width:295px;border:1px solid #ccc}.package-name-wrapper i{float:rig' +
            'ht;cursor:pointer;padding-top:7px;color:#999;font-size:9px;padding-right:3px}.imports-wrapper div spa' +
            'n{cursor:default}.import-declaration-wrapper{float:left;margin:2px 3px;border:1px solid orange;font-s' +
            'ize:12px}.import-wrapper-heading input[type=text]{width:276px}#import-package-text{width:90%;backgrou' +
            'nd-color:#fff!important;line-height:18px;border:0 solid #ccc;font-size:14px;padding:2px 10px;color:#3' +
            '33;font-style:normal}.service-annotation-wrapper-heading{background-color:#3d3d3d}.service-annotation' +
            '-wrapper-heading[disabled]{border-left:1px solid #000}.service-annotation-wrapper-heading[disabled],.' +
            'service-annotation-wrapper-heading[disabled] *{background-color:#eee;opacity:1;cursor:not-allowed}.se' +
            'rvice-annotation-wrapper-heading input[type=text]{width:224px;margin-left:5px}.service-annotation-wra' +
            'pper-heading select{width:160px}.service-annotation-main-action-wrapper{width:435px;overflow:auto;dis' +
            'play:none;background:#fff}.action-content-dropdown-wrapper ul{position:absolute;top:inherit}.service-' +
            'annotation-details-wrapper{padding:0;overflow-y:scroll;width:100%;border-left:1px solid #555;border-b' +
            'ottom:1px solid #555;max-height:450px}.service-annotation-details-wrapper hr{margin-left:8px;margin-r' +
            'ight:5px}.service-annotation-detail-wrapper{overflow:hidden;padding:4px 5px 3px 12px;cursor:pointer}.' +
            'service-annotation-detail-wrapper:hover{background-color:#f5f5f5}.service-annotation-detail-wrapper:h' +
            'over .service-annotation-detail-close-wrapper{display:block}.service-annotation-detail-type-wrapper{f' +
            'loat:left;width:120px;font-weight:700}.service-annotation-detail-value-wrapper{float:left}.service-an' +
            'notation-detail-value-wrapper textarea{width:280px;float:left}.service-annotation-detail-close-wrappe' +
            'r{float:right;font-size:10px;padding-top:3px;color:#999;display:none}.svg-action-content-wrapper-body' +
            '{background:#fff;border:1px solid #f0f0f0}.svg-action-content-wrapper-heading{fill:#eaeaea}.variables' +
            '-content-wrapper,attachment-content-wrapper,.variables-action-wrapper,.attachment-action-wrapper{floa' +
            't:left}.attachment-add-icon-wrapper{height:16px;position:static;width:15px;font-size:11px;float:left;' +
            'margin:7px 2px 0 7px;opacity:1}.attachment-add-action-wrapper{display:none;width:517px;background-col' +
            'or:#ddd;height:26px;padding:2px 4px}.attachment-add-action-wrapper input{width:88%;margin-left:4px;he' +
            'ight:22px;padding-left:5px;border:1px solid #dfdfdf}.variable-add-action-wrapper{display:none;width:5' +
            '15px;background-color:#ddd;height:28px;padding:2px 4px}.variable-add-action-wrapper input{width:163px' +
            ';margin-left:2px;height:22px;padding-left:5px;border:1px solid #dfdfdf}.variable-add-complete-action-' +
            'wrapper,.attachment-add-complete-action-wrapper{height:22px;position:static;float:right}.variable-add' +
            '-cancel-action-wrapper,.attachment-add-cancel-action-wrapper{height:22px;position:static;float:right}' +
            '.variable-wrapper,.attachment-wrapper{float:left;border:1px solid #19b89c;margin:2px;font-size:12px;h' +
            'eight:22px}.attachment-wrapper{border:1px solid #000005}.constants-btn:hover{opacity:1;cursor:pointer' +
            '}.constants-action-wrapper,.imports-action-wrapper{display:none}.constants-wrapper,.imports-wrapper{b' +
            'order:1px solid #ddd;background-color:#fff;margin:auto;top:-28px;left:69px;position:relative;float:le' +
            'ft;min-height:28px}.constants-wrapper,.constants-content-wrapper,.constants-action-wrapper,.imports-w' +
            'rapper,.imports-action-wrapper{float:left}.imports-content-wrapper{float:none}.constant-add-icon-wrap' +
            'per,.import-add-icon-wrapper{height:28px;position:static;width:26px;font-size:11px;float:left;text-al' +
            'ign:center;padding-top:9px;margin:-1px;color:#fff;background:#9e9e9e;opacity:1}.constant-add-action-w' +
            'rapper,.import-add-action-wrapper{display:none;width:520px;background-color:#ddd;height:26px;padding:' +
            '2px 4px}.constant-add-action-wrapper input{width:163px;margin-left:4px;height:22px;padding-left:5px;b' +
            'order:0}.constant-add-complete-action-wrapper,.import-add-complete-action-wrapper{height:22px;positio' +
            'n:static;float:right}.constant-add-cancel-action-wrapper,.import-add-cancel-action-wrapper{height:22p' +
            'x;position:static;float:right;margin-right:-3px}.constant-wrapper{float:left;margin-left:6px;margin-b' +
            'ottom:3px}.attachment-pane{border:1px solid #ddd;background-color:#fff;position:absolute;top:9px;left' +
            ':28px;margin:auto;min-height:28px}.constant-pane-collapser-wrapper,.import-pane-collapser-wrapper,.va' +
            'riable-pane-collapser-wrapper,.attachment-pane-collapser-wrapper{font-size:12px;padding:9px;backgroun' +
            'd-color:#efefef;color:#fff;height:28px;width:23px;margin-left:4px;position:absolute;right:-24px;top:-' +
            '1px;border-left:none}.constant-pane-collapser-wrapper{background-color:#9b59b6}.import-pane-collapser' +
            '-wrapper{background-color:#f39c12}.attachment-pane-collapser-wrapper{margin-left:4px;height:27px;righ' +
            't:-23px;top:0;background-color:#000005;padding:9px}.variable-pane-collapser-wrapper{background-color:' +
            '#3498db;top:0;right:-21px}.variable-pane-collapser-wrapper i{position:absolute;vertical-align:top;top' +
            ':8px;right:7px}.attachment-pane-collapser-wrapper:hover{cursor:pointer;opacity:1}.attachment-pane-col' +
            'lapser-wrapper i{font-size:12px;position:absolute;vertical-align:top;top:8px;right:7px;opacity:.5}.co' +
            'nstant-pane-collapser-wrapper:hover{cursor:pointer;opacity:1}.constant-pane-collapser-wrapper i{posit' +
            'ion:absolute;vertical-align:top;top:8px;right:7px}.constant-declaration-wrapper i,.import-declaration' +
            '-wrapper i{padding:6px;font-size:8px;background-color:#fff;color:#8e44ad;border-left-width:0;float:le' +
            'ft;opacity:.5}.import-declaration-wrapper i{color:orange}.constant-declaration-type{background-color:' +
            '#8e44ad;float:left;color:#fff;padding:2px 8px}.constant-declaration-wrapper{float:left;margin:2px;bor' +
            'der:1px solid #8e44ad;font-size:12px;height:22px}.constant-declaration-wrapper i:hover,.import-declar' +
            'ation-wrapper i:hover{cursor:pointer;color:#000}.constant-declaration-field{background-color:#FFF;flo' +
            'at:left;padding:1px 8px;margin-top:1px}.constant-declaration-equals{padding-left:3px;padding-right:3p' +
            'x;cursor:default}.constants-definition-wrapper{padding-bottom:10px;float:left;width:89%}.hoverable{op' +
            'acity:.4;cursor:pointer;height:27px;padding-top:6px}.hoverable:hover{opacity:.9;background-color:#333' +
            ';color:#fff}.name-container-div{position:absolute;display:table}.name-span{display:table-cell;vertica' +
            'l-align:middle;font-weight:300;font-size:14px}.canvas-operations-wrapper{float:right}.annotation-icon' +
            '{margin-top:8px;cursor:pointer}.annotation-icon:hover{color:#000}.canvas-operations-separator{-webkit' +
            '-transform:scale(1,2);-moz-transform:scale(1,2);-ms-transform:scale(1,2);-o-transform:scale(1,2);tran' +
            'sform:scale(1,2);transform:scale(1,2);margin-top:6px;color:#d8d8d8;font-size:14px;font-weight:300}.se' +
            'rvice-annotation-action-icon{top:7px;right:6px}.service-annotation-action-icon-i{background-color:#81' +
            '8181}.operations-annotation-icon{color:#FFF;opacity:1;background-color:#2b2b2b;width:30px}.statement-' +
            'title-rect{fill:#FFF;stroke:#818181}.statement-title-rect-error{fill:#FFF;stroke:#ff0202}.statement-t' +
            'itle-polyline{fill:none;stroke:#000}.statement-title-polyline-error{fill:none;stroke:#ff0202}.argumen' +
            'ts-main-action-wrapper{width:435px;overflow:auto;display:none;background:#fff}.arguments-wrapper-head' +
            'ing{background-color:#3d3d3d}.arguments-wrapper-heading input[type=text]{width:224px;margin-left:5px}' +
            '.arguments-wrapper-heading select{width:160px}.arguments-action-icon{top:7px;right:6px}.arguments-act' +
            'ion-icon-i{background-color:#818181}.arguments-details-wrapper{padding:0;overflow-y:scroll;width:100%' +
            ';border-left:1px solid #555;border-bottom:1px solid #555;max-height:450px}.arguments-details-wrapper ' +
            'hr{margin-left:8px;margin-right:5px}.arguments-detail-wrapper{overflow:hidden;padding:4px 5px 3px 12p' +
            'x;cursor:pointer}.arguments-detail-wrapper:hover{background-color:#f5f5f5}.arguments-detail-wrapper:h' +
            'over .arguments-detail-close-wrapper{display:block}.arguments-detail-close-wrapper{float:right;font-s' +
            'ize:10px;padding-top:3px;color:#999;display:none}.arguments-detail-type-wrapper{float:left;width:120p' +
            'x;font-weight:700}.arguments-detail-identifier-wrapper{float:left}.arguments-detail-identifier-wrappe' +
            'r input{padding-left:8px;padding-right:8px;width:280px;height:22px}.operations-argument-icon{color:#F' +
            'FF;opacity:1;background-color:#2b2b2b;width:30px}#modalAbout .modal-dialog{width:35%}#modalAbout .mod' +
            'al-footer{text-align:left;padding-top:5px}#modalAbout .modal-footer .icon{font-size:35px;vertical-ali' +
            'gn:middle}#modalAbout .modal-footer a{text-decoration:none}#modalSearch .modal-dialog{width:60%}#moda' +
            'lSearch input{color:#000}.modal-search-icon{position:absolute;right:20px;top:8px;color:#7b7b7b}.searc' +
            'h-results{overflow:auto;background-color:#2C2C2C;margin-top:10px}.search-results ul li{background-col' +
            'or:#2C2C2C;border:0;cursor:pointer}.search-results ul li:hover{background-color:#222;color:#ff7916}.s' +
            'truct-title-text:hover{cursor:text}.type-mapper-title-text:hover{cursor:text}.type-struct-title-text:' +
            'hover{cursor:text}.canvas-title:hover{cursor:text}.service-left-scroll{cursor:pointer;position:absolu' +
            'te;top:0;left:0;width:50px;background-color:#CCC;opacity:.3}.service-left-scroll:hover{opacity:.5}.se' +
            'rvice-left-scroll i{font-size:45px;padding-left:13px}.service-right-scroll{cursor:pointer;position:ab' +
            'solute;top:0;right:0;width:50px;background-color:#CCC;opacity:.3}.service-right-scroll:hover{opacity:' +
            '.5}.service-right-scroll i{font-size:45px;padding-left:13px}#breadcrumb-container > .breadcrumb{text-' +
            'transform:none;background:transparent}#breadcrumb-container > ol > li.breadcrumb-item{color:grey;curs' +
            'or:pointer}#breadcrumb-container > ol > li.breadcrumb-item.active{color:#fff;cursor:pointer}footer > ' +
            '.container p,footer > .container-fluid p{line-height:0;font-size:12px;margin:0}header .brand span{fon' +
            't-size:15px;font-weight:400}a{color:#333;text-decoration:none}a:hover{text-decoration:none}.panel-bod' +
            'y{padding:0}.panel-default{border:0}.annotation-body{padding:0}.sidebar-left{background:#333}.modal-d' +
            'ialog.file-dialog{margin:100px auto 0}.modal-dialog.file-dialog .jstree-anchor.jstree-clicked{color:i' +
            'nherit}.delete-item-dialog .icon{float:left;margin-left:25px;margin-top:5px}.delete-item-dialog .text' +
            '{margin-left:150px}.tool-group-import-header > a.tool-group-header-title{color:#fff}i.fw-lg.fw.fw-can' +
            'cel.about-dialog-close{font-size:12px}i.fw.fw-cancel.about-dialog-close{font-size:12px}ul.list-group{' +
            'margin-bottom:0}span.fw-stack.fw-lg{z-index:10}.import-img{height:37px;width:37px}.constants-btn,.pac' +
            'kage-name-btn,.imports-btn,.variable-btn,.attachments-btn{background:#eee;width:69px;color:#000005;pa' +
            'dding:5px 10px;height:28px;font-size:12px;opacity:1;border-left-width:3px;border-left-style:solid}.co' +
            'nstants-btn{border-left-color:#9b59b6}.package-name-btn{border-left-color:#16a085}.imports-btn{border' +
            '-left-color:#f39c12}.variable-btn{border-left-color:#3498db}.attachments-btn{position:absolute;width:' +
            '90px}.import-declaration-type{float:left;background:orange;width:3px;height:21px;margin-right:4px}.im' +
            'port-package-name{float:left;margin-top:3px;font-size:12px}.package-name-btn{float:left}.constants-bt' +
            'n:hover,.package-name-btn:hover{cursor:pointer;opacity:1}.constants-add-icon-wrapper,.package-add-ico' +
            'n-wrapper,.imports-add-icon-wrapper,.variable-add-icon-wrapper{float:left;color:#fff;min-height:28px;' +
            'border-left:2px solid #ddd;width:25px;padding-top:8px;text-align:center;font-size:12px}.package-add-i' +
            'con-wrapper{background-color:#16a085}.variable-add-icon-wrapper{background-color:#989898;height:16px;' +
            'position:static;width:28px;font-size:11px;float:left}.left-tool-bar .nav-tabs li.active a{background:' +
            '#333!important;border-left:3px solid #f17b31!important}.panel-default .panel-title{font-weight:300;fo' +
            'nt-size:14px}.orange{color:#f17b31}.select2-container--default .select2-selection--single{border-radi' +
            'us:0!important}.topbar{display:none}.SplitPane .vertical{background-color:#FFF}.swagger-ui h1,h2,h3,h' +
            '4,h5,h6,.h1,.h2,.h3,.h4,.h5,.h6{font-weight:400;line-height:normal}.swagger-ui pre{padding:0;font-siz' +
            'e:medium;line-height:normal;word-break:normal;word-wrap:normal;background-color:transparent;border-ra' +
            'dius:0}.expand-operation{display:none}.SplitPane .vertical{overflow:auto}.swaggerEditor .SplitPane{he' +
            'ight:100%!important}.try-out{display:none}.swagger-service-selector-wrapper{background-color:#131313;' +
            'height:22px;overflow:hidden;line-height:15px;color:#fff;padding-left:20px}.swagger-service-selector-s' +
            'pan{float:left;margin-top:6px;padding-right:8px}.annotation-editor-view{position:absolute!important;z' +
            '-index:100!important}.media-welcome-container > .media-body > .welcome-details-wrapper > .details-con' +
            'tainer .thumbnail-wrapper > .thumbnail.multiple:before{font-family:"font-wso2";-webkit-font-smoothing' +
            ':antialiased}.media-welcome-container > .media-body > .welcome-details-wrapper > .details-container .' +
            'thumbnail-wrapper > .thumbnail.multiple:before b{height:0;display:block;text-indent:100%;white-space:' +
            'nowrap;overflow:hidden}.header.header-default{background-color:#16191d;min-height:27px}.header.header' +
            '-default .brand{color:#fff}.header.header-default .brand a{color:#fff}.header.header-default .brand s' +
            'pan.appname{color:#f17b31}.menu-bar .menu-group > a{color:#c7c7c7}.menu-bar .menu-group > a:hover{bac' +
            'kground-color:#16191d;color:#eee}.menu-bar .menu-group > .file-dropdown-menu{background-color:#565656' +
            ';margin-top:17px;width:210px}.menu-bar.menu-group > .file-dropdown-menu > li > a:hover,.menu-bar .men' +
            'u-group > .file-dropdown-menu > li > a:focus{text-decoration:none;color:#565656;background-color:#d4d' +
            '4d4;cursor:pointer}.menu-bar .menu-group > .file-dropdown-menu > li.menu-item-disabled > a{color:#afa' +
            'faf;padding:3px 10px;height:27px}.menu-bar .menu-group > .file-dropdown-menu > li.menu-item-disabled ' +
            '> a > .shortcut-label{color:#afafaf}.menu-bar .menu-group > .file-dropdown-menu > li.menu-item-disabl' +
            'ed > a:hover,.menu-bar .menu-group > .file-dropdown-menu > li.menu-item-disabled > a:focus{color:#afa' +
            'faf;background-color:#565656;cursor:default}body.sticky-footer footer{position:absolute;bottom:0;z-in' +
            'dex:1000}body footer{background:#282b2f;color:#cbcbcb}#breadcrumb-container{background-color:#151515;' +
            'height:28px;overflow:hidden;line-height:15px}#breadcrumb-container > ol.breadcrumb > li.breadcrumb-it' +
            'em.active{color:#fff}.left-tool-bar{height:calc(100% - 100px);padding:0;width:40px;position:fixed;fon' +
            't-size:12px;text-align:center;z-index:2;background:#151515}.left-tool-bar .nav-tabs > li i{color:#999' +
            ';opacity:.6}.tab-container{background-color:#fff}.tab-headers{background-color:#282828}.tab-headers >' +
            ' ul > li{min-width:120px;color:#999;font-weight:100;margin:0;padding-right:17px;padding-left:5px;bord' +
            'er-right:1px;background:#565656;border-left:1px solid #333}.tab-headers > ul > li.active{background:#' +
            'fff;color:#555;border-left:3px solid #f17b31}.tab-headers > ul > li.active.inverse{color:#bebebe;back' +
            'ground:#3c3c3c}#tab-content-wrapper{position:relative}.bottom-right-controls-container{position:absol' +
            'ute;bottom:0;right:30px;z-index:30;background:#707276;color:#fff;padding:0;line-height:30px}body .edi' +
            'tor-wrapper{height:100%}body .ace-twilight{background-color:#3c3c3c;color:#F8F8F8}body .ace-twilight ' +
            '.ace_gutter{background:#313131;color:#E2E2E2}body .tooltip.tool-palette{display:none!important}body.t' +
            'ool-palette-view-tiles .tooltip.tool-palette{display:inherit!important}.tool-palette-container{backgr' +
            'ound:#fff}.tool-palette-container .tool-group-header{background:#eee}.tool-palette-container .tool-gr' +
            'oup-header > a{color:#333}.tool-palette-container .tool-group-header > a:hover{color:#f17b31}.tool-pa' +
            'lette-container .tool-group-body .tools-view-modes-controls{margin-bottom:10px}.tool-palette-containe' +
            'r .tool-group-body .tool-container{width:39px;height:39px;margin:3px;cursor:pointer;border:1px solid ' +
            '#eee;padding:3px;white-space:nowrap;text-align:center}.tool-palette-container .tool-group-body .tool-' +
            'container .icon{color:#666769;font-size:24px;margin-top:3px;vertical-align:text-bottom}.tool-palette-' +
            'container .tool-group-body .tool-container:hover .icon,.tool-palette-container .tool-group-body .tool' +
            '-container:hover .tool-title{color:#f17b31}.tool-palette-container .tool-group-body .tool-separator{w' +
            'idth:100%;border-bottom:1px solid #eee;clear:both;padding-top:12px;margin-bottom:12px}.tool-palette-c' +
            'ontainer .tool-group-body.tool-group-body-list{margin-left:-4px}.tool-palette-container .tool-group-b' +
            'ody.tool-group-body-list .tool-container{height:28px;text-align:left;border:none}.tool-palette-contai' +
            'ner .tool-group-body.tool-group-body-list .tool-container .icon{font-size:16px}.tool-palette-containe' +
            'r .tool-group-body.tool-group-body-list .tool-container .tool-title{margin-top:-1px;vertical-align:te' +
            'xt-top;margin-left:8px;font-size:.94em}.tool-palette-container .tool-group-body.tool-group-body-list ' +
            '.tooltip{display:none!important}.tool-palette-container .tool-group-body.tool-group-body-list .tool-c' +
            'ontainer-vertical .tool-container-vertical-icon{width:25px;height:25px;cursor:pointer;border:1px soli' +
            'd #eee;padding:3px;white-space:nowrap;text-align:center;float:left}.tool-palette-container .tool-grou' +
            'p-body.tool-group-body-list .tool-container-vertical .tool-container-vertical-icon .tool-image{margin' +
            '-top:-3px}.tool-palette-container .tool-group-body.tool-group-body-list .tool-container-vertical .too' +
            'l-container-vertical-title{color:#666769}.tool-palette-container .tool-group-body.tool-group-body-lis' +
            't .tool-container-vertical .tool-title{display:none}.tool-palette-container .tool-group-body.tool-gro' +
            'up-body-list .tool-container-vertical:hover .tool-container-vertical-title{color:#f17b31}.tool-palett' +
            'e-container .tool-group-body .tool-title{color:#666769;font-size:.94em;display:none}.tool-palette-con' +
            'tainer .tool-group-body.tool-group-body-grid .icon{font-size:24px}.tool-palette-container .tool-group' +
            '-body.tool-group-body-grid .tool-title{color:#666769;display:inline-block}.tool-palette-container .to' +
            'ol-group-body.tool-group-body-grid .tooltip{display:none!important}.theme-chooser .theme-pick{border:' +
            '2px solid #4a4a4a;margin-right:5px;margin-top:5px;height:30px;width:30px;display:inline-block}.theme-' +
            'chooser .theme-pick > .square{display:block;height:26px;width:26px}.theme-chooser .theme-pick > .squa' +
            're.light{background:#f5f5f5}.theme-chooser .theme-pick > .square.dark{background:#000}.theme-chooser ' +
            '.theme-pick > .square.default{background:#f5f5f5;border-top:8px solid #000;border-left:8px solid #000' +
            '}.panel .panel-header > rect{fill:#dcdcdc;stroke:#dcdcdc;stroke-width:2px}.panel .panel-header > .ann' +
            'otation-icon-wrapper{fill:#f4f4f4;shape-rendering:crispEdges;stroke:#f4f4f4}.panel .panel-header > te' +
            'xt{fill:#333}.panel .panel-header > .panel-heading-decorator{width:3px;fill:#3498db;stroke-width:0}.p' +
            'anel .panel-body .panel-body-rect{fill:#fff;fill-opacity:.4;stroke-width:2px;stroke:#dcdcdc}.panel .p' +
            'anel-body .panel-body-rect.rect.drop-zone{fill:#fff;fill-opacity:0}.default-worker-life-line{stroke:#' +
            '0070af;stroke-width:2;cursor:pointer}.default-worker-life-line-polygon{fill:#0070af;stroke-width:0}.w' +
            'orker-life-line{cursor:pointer;stroke:#0b91dc;stroke-width:2}.worker-life-line-polygon{fill:#0b91dc;s' +
            'troke-width:0}.connector-life-line{cursor:pointer;stroke:#26A69A;stroke-width:2}.connector-life-line-' +
            'polygon{fill:#26A69A;stroke-width:0}.life-line-text{fill:#f1f1f1;stroke:#f1f1f1;stroke-width:.5}.impo' +
            'rt-definition-decorator{width:3px;fill:#f17b31}.import-badge{fill:#f17b31}.import-badge-text{fill:#ff' +
            'f}.global-definition-decorator{width:3px;fill:#9b59b6}.global-badge{fill:#9b59b6}.global-badge-text{f' +
            'ill:#fff}.package-declaration-item .background,.package-declaration-item .delete-background,.global-d' +
            'efinition-item .background,.global-definition-item .delete-background{fill:#eee}.add-import-button-ba' +
            'ckground,.add-import-button,.add-global-button-background,.add-global-button{fill:#eee}.package-defin' +
            'ition-header{fill:#eee;cursor:pointer}.package-definition-head{cursor:pointer}.package-definition-lab' +
            'el{dominant-baseline:central;fill:#000}.annotation-container{background:#f4f4f4;overflow:auto;padding' +
            ':7px;overflow:visible}.annotation-table td{height:20px;line-height:10px}.annotation-table td .react-a' +
            'utosuggest__container{line-height:15px}.annotation-key .package-name{font-weight:500;color:#ea940c}.r' +
            'eact-autosuggest__suggestions-list{margin:0;padding:0;list-style-type:none;height:350px}.react-autosu' +
            'ggest__suggestion{padding:4px}.react-autosuggest__suggestion.react-autosuggest__suggestion--highlight' +
            'ed,.react-autosuggest__suggestion:hover{background:#333;color:#eee;cursor:pointer}.annotationRect{fil' +
            'l:#eee;shape-rendering:crispEdges;stroke:#ccc;stroke-width:.5}.annotation-text{font-size:.9em;fill:#6' +
            '66769}.action-dash-line{stroke:#666769;fill:#666769;stroke-dasharray:5,5}.action-arrow,.action-arrow-' +
            'head{stroke:#666769;fill:#666769}.struct-content-operations-wrapper{padding:10px;float:left;fill:#ddd' +
            ';stroke-width:0}.struct-type-dropdown-wrapper,.struct-input-value-wrapper{fill:#f5f5f5;stroke-width:1' +
            '.5;stroke:#ccc;shape-rendering:crispEdges;stroke-dasharray:3,3}.struct-add-icon-wrapper{cursor:pointe' +
            'r;opacity:.5}.struct-add-icon-wrapper:hover{cursor:pointer;opacity:1}.struct-input-text{dominant-base' +
            'line:middle;fill:#707276}.struct-input-text:hover{dominant-baseline:middle;fill:#333}.struct-added-va' +
            'lue-wrapper{fill:#f3f3f3;stroke-width:.5;stroke:#dfdfdf;shape-rendering:crispEdges}.parameter-delete-' +
            'icon-wrapper{fill:#8ebadd;shape-rendering:crispEdges;padding:13px}.parameter-delete-icon-wrapper:hove' +
            'r{opacity:1;fill:#dcdcdc}.struct-delete-icon-wrapper{fill:#f3f3f3;shape-rendering:crispEdges;padding:' +
            '13px}.struct-delete-icon-wrapper:hover{opacity:1;fill:#dcdcdc}.argument-parameter-group,.annotation-a' +
            'ttachment-group,.return-parameter-group{fill:#dcdcdc;height:30px}.parameter-bracket-text,.return-type' +
            's-opening-brack-text,.return-types-closing-brack-text{fill:#999;dominant-baseline:text-before-edge}.p' +
            'arameter-prefix-text,.parameter-text{dominant-baseline:text-before-edge;cursor:default}.parameter-wra' +
            'pper{fill:#8ebadd;shape-rendering:crispEdges}.annotation-icon{opacity:.5}.join-lifeline{stroke:#000;s' +
            'troke-width:1}.grid-background{background-color:#fff!important;background:linear-gradient(-90deg,#eee' +
            ' 1px,transparent 1px),linear-gradient(#eee 1px,transparent 1px),linear-gradient(-90deg,#eee 1px,trans' +
            'parent 1px),linear-gradient(#eee 1px,transparent 1px),linear-gradient(transparent 3px,transparent 3px' +
            ',transparent 78px,transparent 78px),linear-gradient(-90deg,transparent 1px,transparent 1px),linear-gr' +
            'adient(-90deg,transparent 3px,transparent 3px,transparent 78px,transparent 78px),linear-gradient(tran' +
            'sparent 1px,transparent 1px),transparent;background-size:10px 10px,10px 10px,10px 10px,10px 10px,10px' +
            ' 10px,10px 10px,10px 10px,10px 10px}.docerina-page{width:100%;height:100%;overflow:hidden}.initial-do' +
            'c-background-container{width:100%;height:100%;overflow:hidden}.media-welcome-container{width:100%;pad' +
            'ding:20px}.media-welcome-container > .media-left{width:300px;padding:20px 40px 20px 20px;border-right' +
            ':1px solid #ccc;height:auto}.media-welcome-container > .media-left > .logo{width:200px;padding:10px;m' +
            'argin-bottom:30px}.media-welcome-container > .media-left .btn{width:200px;height:37px;border:0;margin' +
            ':15px 0 0;color:#fff;font-size:16px}.media-welcome-container > .media-left .btn.btn-primary{backgroun' +
            'd-color:#f17b31}.media-welcome-container > .media-left .btn.btn-secondary{background-color:#484848}.m' +
            'edia-welcome-container > .media-left > .nav-pills{margin-top:20px}.media-welcome-container > .media-l' +
            'eft > .nav-pills > li{display:block;width:100%;padding:5px;margin:0}.media-welcome-container > .media' +
            '-left > .nav-pills > li a{padding:0;font-weight:400}.media-welcome-container > .media-left > .nav-pil' +
            'ls > li a:hover,.media-welcome-container > .media-left > .nav-pills > li a:focus{background-color:#ff' +
            'f;color:#f17b31}.media-welcome-container > .media-body{width:100%;padding:20px 20px 0}.media-welcome-' +
            'container > .media-body > .welcome-details-wrapper > .header-title{font-size:18px;font-weight:400;mar' +
            'gin-left:20px}.media-welcome-container > .media-body > .welcome-details-wrapper > .details-container{' +
            'margin-top:10px;background-color:rgba(228,228,228,0.5);padding:10px;margin-bottom:25px;margin-left:20' +
            'px}.media-welcome-container > .media-body > .welcome-details-wrapper > .details-container .thumbnail-' +
            'wrapper{padding:0;min-height:205px}.media-welcome-container > .media-body > .welcome-details-wrapper ' +
            '> .details-container .thumbnail-wrapper > .thumbnail{background-color:#fff;min-height:100px;margin:15' +
            'px;cursor:pointer}.media-welcome-container > .media-body > .welcome-details-wrapper > .details-contai' +
            'ner .thumbnail-wrapper > .thumbnail.multiple{box-shadow:0 .0625em .1875em 0 rgba(0,0,0,0.1),0 .5em 0 ' +
            '-.25em #fff,0 .5em .1875em -.25em rgba(0,0,0,0.1),0 1em 0 -.5em #fff,0 1em .1875em -.5em rgba(0,0,0,0' +
            '.1);position:relative}.media-welcome-container > .media-body > .welcome-details-wrapper > .details-co' +
            'ntainer .thumbnail-wrapper > .thumbnail.multiple:before{content:"\E729";position:absolute;bottom:10px' +
            ';right:10px}.media-welcome-container > .media-body > .welcome-details-wrapper > .details-container .t' +
            'humbnail-wrapper > .thumbnail > img{max-width:90%}.media-welcome-container > .media-body > .welcome-d' +
            'etails-wrapper > .details-container .thumbnail-wrapper > .thumbnail > .caption{padding:10px 10px 0}.m' +
            'edia-welcome-container > .media-body > .welcome-details-wrapper > .details-container .thumbnail-wrapp' +
            'er > .thumbnail > .caption > h4{font-size:12pt;color:#4a4a4a;font-weight:400;margin:5px 0}.media-welc' +
            'ome-container > .media-body > .welcome-details-wrapper > .details-container .thumbnail-wrapper > .thu' +
            'mbnail > .caption > p{font-size:12px;color:#7b7b7b;margin:0}.modal-content .btn-default{background:#2' +
            'c2c2c;color:#fff;text-transform:capitalize}.modal-content .btn-default:hover{background:#464646;color' +
            ':#fff}.modal-content .btn-primary{background-color:#F57E30;border-color:#F57E30;color:#fff}.modal-con' +
            'tent .btn-primary:hover{background-color:#e7620b;border-color:#e7620b;color:#fff}.modal-content a{col' +
            'or:#F57E30}.modal-content .brand .logo{width:150px;vertical-align:top}.modal-content .brand .appname{' +
            'color:#f17b31;font-size:39px;display:inline-block;margin-top:-11px;margin-left:2px}.transform-action ' +
            '{cursor: pointer;text-anchor: middle;dominant-baseline: central;width: 120px;max-width: 250px;}.proto' +
            'col-line{stroke:#607D8B}.protocol-rect{fill:#607D8B;fill-opacity:.5}.protocol-text{font-weight:400}.p' +
            'rotocol-http .protocol-line{stroke:#3498db}.protocol-http .protocol-rect{fill:#3498db}.protocol-ws .p' +
            'rotocol-line{stroke:#E65100}.protocol-ws .protocol-rect{fill:#E65100}.protocol-jms .protocol-line{str' +
            'oke:#558B2F}.protocol-jms .protocol-rect{fill:#558B2F}.protocol-file .protocol-line{stroke:#966c37}.p' +
            'rotocol-file .protocol-rect{fill:#966c37}.panel-heading-decorator{fill:#607D8B}.protocol-http .panel-' +
            'heading-decorator{fill:#3498db}.protocol-ws .panel-heading-decorator{fill:#E65100}.protocol-jms .pane' +
            'l-heading-decorator{fill:#558B2F}.protocol-file .panel-heading-decorator{fill:#966c37;fill:#966c37!im' +
            'portant}.protocol-jms .panel-heading-decorator{fill:#558B2F!important}.protocol-ws .panel-heading-dec' +
            'orator{fill:#E65100!important}.protocol-http .panel-heading-decorator{fill:#3498db!important}.panel-h' +
            'eading-decorator{fill:#607D8B!important}';
        this.app = options;
        this.dialogContainer = _.get(options.config.dialog, "container");
        this.notificationContainer = _.get(options.config.tab_controller.tabs.tab.ballerina_editor.notifications,
            'container');
    },

    /**
     * Show export diagram modal.
     * */
    show() {
        this._exportDiagramModal.modal('show').on('shown.bs.modal', () => {
            this.trigger('loaded');
        });
        this._exportDiagramModal.on('hidden.bs.shown', () => {
            this.trigger('unloaded');
        });
    },

    /**
     * Set diagram export path.
     *
     * @param {object} activeTab - active tab object.
     * @param {string} path - active file path.
     * @param {fileName} fileName - file name.
     * */
    setDiagramPath(activeTab, path, fileName) {
        this._fileBrowser.select(path);
        this._activeTabId = activeTab.id;
        if (!_.isNil(this._configNameInput)) {
            if (fileName.endsWith('.bal')) {
                fileName = fileName.replace('.bal', '');
            }
            this._configNameInput.val(fileName);
        }
    },

    /**
     * Render the export modal.
     * */
    render() {
        const self = this;
        let fileBrowser;
        const app = this.app;
        const notificationContainer = this.notificationContainer;
        const css = this.css;
        if (!_.isNil(this._exportDiagramModal)) {
            this._exportDiagramModal.remove();
        }

        const fileSave = $(
            "<div class='modal fade' id='saveConfigModal' tabindex='-1' role='dialog' aria-tydden='true'>" +
            "<div class='modal-dialog file-dialog' role='document'>" +
            "<div class='modal-content'>" +
            "<div class='modal-header'>" +
            "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
            "<span aria-hidden='true'>&times;</span>" +
            '</button>' +
            "<h4 class='modal-title file-dialog-title' id='newConfigModalLabel'>Export Diagram</h4>" +
            "<hr class='style1'>" +
            '</div>' +
            "<div class='modal-body'>" +
            "<div class='container-fluid'>" +
            "<form class='form-horizontal' onsubmit='return false'>" +
            "<div class='form-group'>" +
            "<label for='configName' class='col-sm-2 file-dialog-label'>File Name :</label>" +
            "<div class='col-sm-9'>" +
            "<input class='file-dialog-form-control' id='configName' placeholder='eg: diagram.svg'>" +
            '</div>' +
            '</div>' +
            "<div class='form-group'>" +
            "<label for='location' class='col-sm-2 file-dialog-label'>Location :</label>" +
            "<div class='col-sm-9'>" +
            "<input type='text' class='file-dialog-form-control' id='location' placeholder='eg: /home/user/wso2-integration-server/ballerina-diagrams'>" +
            '</div>' +
            '</div>' +
            "<div class='form-group'>" +
            "<div class='file-dialog-form-scrollable-block'>" +
            "<div id='fileTree'>" +
            '</div>' +
            "<div id='file-browser-error' class='alert alert-danger' style='display: none;'>" +
            '</div>' +
            '</div>' +
            '</div>' +
            "<div class='form-group'>" +
            "<div class='file-dialog-form-btn'>" +
            "<button id='saveButton' type='button' class='btn btn-primary'>Export" +
            '</button>' +
            "<div class='divider'/>" +
            "<button type='cancelButton' class='btn btn-default' data-dismiss='modal'>Cancel</button>" +
            '</div>' +
            '</div>' +
            '</form>' +
            "<div id='newWizardError' class='alert alert-danger'>" +
            '<strong>Error!</strong> Something went wrong.' +
            '</div>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '</div>',
        );

        const successNotification = $(
            "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-success' id='success-alert'>" +
            "<span class='notification'>" +
            'Diagram successfully exported !' +
            '</span>' +
            '</div>');

        function getErrorNotification(detailedErrorMsg) {
            let errorMsg = 'Error While Exporting Diagram';
            if (!_.isEmpty(detailedErrorMsg)) {
                errorMsg += (`: ${ detailedErrorMsg}`);
            }
            return $(
                `${"<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-danger' id='error-alert'>" +
                "<span class='notification'>"}${
                    errorMsg
                    }</span>` +
                '</div>'
            );
        }

        const exportDiagramModal = fileSave.filter('#saveConfigModal');
        const newWizardError = fileSave.find('#newWizardError');
        const location = fileSave.find('input').filter('#location');
        const configName = fileSave.find('input').filter('#configName');

        const treeContainer = fileSave.find('div').filter('#fileTree');
        fileBrowser = new FileBrowser({
            container: treeContainer,
            application: app,
            fetchFiles: false
        });

        fileBrowser.render();
        this._fileBrowser = fileBrowser;
        this._configNameInput = configName;

        this.listenTo(fileBrowser, 'selected', (selectedLocation) => {
            if (selectedLocation) {
                location.val(selectedLocation);
            }
        });

        function handleExport() {
            const _location = location.val();
            let _configName = configName.val();
            if (_.isEmpty(_location)) {
                newWizardError.text('Please enter a valid file export location');
                newWizardError.show();
                return;
            }
            if (_.isEmpty(_configName)) {
                newWizardError.text('Please enter a valid file name');
                newWizardError.show();
                return;
            }

            if (_configName.endsWith('.bal')) {
                _configName = _configName.replace('.bal', '');
            }

            if (!_configName.endsWith('.svg')) {
                _configName += '.svg';
            }

            const callback = function (isSaved) {
                self.trigger('save-completed', isSaved);
                if (isSaved) {
                    exportDiagramModal.modal('hide');
                }
            };

            const client = self.app.workspaceManager.getServiceClient();
            const path = `${_location}/${_configName}`;
            const existsResponse = client.exists(path);

            if (existsResponse.exists) {
                const replaceConfirmCb = function (confirmed) {
                    if (confirmed) {
                        exportDiagram({
                            location: _location,
                            configName: -_configName
                        }, callback);
                    } else {
                        callback(false);
                    }
                };

                const options = {
                    path,
                    handleConfirm: replaceConfirmCb,
                };

                self.app.commandManager.dispatch('open-replace-file-confirm-dialog', options);
            } else {
                exportDiagram({
                    location: _location,
                    configName: _configName
                }, callback);
            }
        }

        fileSave.find('button').filter('#saveButton').click(() => {
            handleExport();
        });

        function handleKeyPress(e) {
            if (e.keyCode === 13 || e.which === 13) {
                e.stopPropagation();
                e.preventDefault();
                handleExport();
            }
        }

        location.keypress(handleKeyPress);
        configName.keypress(handleKeyPress);

        $(this.dialogContainer).append(fileSave);
        newWizardError.hide();
        this._exportDiagramModal = fileSave;
        function alertSuccess() {
            $(notificationContainer).append(successNotification);
            successNotification.fadeTo(2000, 200).slideUp(1000, () => {
                successNotification.slideUp(1000);
            });
        }

        function alertError(errorMessage) {
            const errorNotification = getErrorNotification(errorMessage);
            $(notificationContainer).append(errorNotification);
            errorNotification.fadeTo(2000, 200).slideUp(1000, () => {
                errorNotification.slideUp(1000);
            });
        }

        /**
         * Get the diagram SVG to export.
         * */
        function getSVG() {
            let svgElement = $("#" + app.tabController.activeTab.id).find('.svg-container');
            let svg = `<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/` +
                `xlink" xml:space="preserve" class="svg-container" width="${svgElement.width()}"` +
                ` height="${svgElement.height()}">`;
            svg += `<style>${css}</style>`;
            svg += svgElement.html();
            svg += "</svg>";
            return svg;
        }

        function isJsonString(str) {
            try {
                JSON.parse(str);
            } catch (e) {
                return false;
            }
            return true;
        }

        function exportDiagram(options, callback) {
            const workspaceServiceURL = app.config.services.workspace.endpoint;
            const saveServiceURL = `${workspaceServiceURL}/write`;
            const config = getSVG();
            const payload = `location=${  btoa(options.location)  }&configName=${  btoa(options.configName)
                }&config=${  encodeURIComponent(config)}`;

            $.ajax({
                url: saveServiceURL,
                type: 'POST',
                data: payload,
                contentType: 'text/plain; charset=utf-8',
                async: false,
                success(data, textStatus, xhr) {
                    if (xhr.status === 200) {
                        exportDiagramModal.modal('hide');
                        log.debug('Diagram successfully exported');
                        callback(true);
                    } else {
                        newWizardError.text(data.Error);
                        newWizardError.show();
                        callback(false);
                    }
                },
                error(res, errorCode, error) {
                    let msg = _.isString(error) ? error : res.statusText;
                    if (isJsonString(res.responseText)) {
                        let resObj = JSON.parse(res.responseText);
                        if (_.has(resObj, 'Error')) {
                            msg = _.get(resObj, 'Error');
                        }
                    }
                    newWizardError.text(msg);
                    newWizardError.show();
                    callback(false);
                },
            });
        }
    },
});

export default ExportDiagramDialog;