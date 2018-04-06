import { fetchConfigs, parseContent, parseFile } from 'api-client/api-client';
import TreeBuilder from 'plugins/ballerina/model/tree-builder.js';

global.TreeBuilder = TreeBuilder;
global.fetchConfigs = fetchConfigs;
global.parseContent = parseContent;
global.parseFile = parseFile;
