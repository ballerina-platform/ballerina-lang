import { fetchConfigs, parseContent } from '@ballerina-lang/composer-api-client';
import TreeBuilder from '../model/tree-builder.js';

global.TreeBuilder = TreeBuilder;
global.fetchConfigs = fetchConfigs;
global.parseContent = parseContent;
