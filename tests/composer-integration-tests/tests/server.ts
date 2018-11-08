import { sync as globSync } from 'glob';
import { moveSync } from 'fs-extra';
import * as path from 'path';

const extractPath = path.join(__dirname, '..', 'target', 
            'extracted-distributions');

export const balToolsPath = path.join(extractPath, 'ballerina-tools');

globSync(path.join(extractPath, `ballerina-tools-*`)).forEach((folder) => {
    if (folder.includes('ballerina-tools')) {
        moveSync(folder, balToolsPath);
    }
});