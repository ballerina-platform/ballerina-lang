const { getProjectVersion } = require('./common');
const { execSync } = require('child_process');
const { join } = require('path');

const projectVersion = getProjectVersion();

execSync("npx lerna version " + projectVersion + " --yes --no-git-tag-version", { cwd: join(__dirname, "..")});
console.log("updated project version" + projectVersion);