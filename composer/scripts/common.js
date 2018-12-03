const fs = require('fs');
const path = require('path');
const resolve = require('path').resolve;
const cp = require('child_process');
const os = require('os');
const parser = require('fast-xml-parser');

// npm cmd based on OS
const npmCmd = os.platform().startsWith('win') ? 'npm.cmd' : 'npm';

// submodule roots
const modulesRoots = [resolve(__dirname, './../packages'), resolve(__dirname, './../tools')];


function runNpmCommandSync(args, cwd) {
  // execute command sync
  const spawnResult = cp.spawnSync(npmCmd, args, { env: process.env, cwd, stdio: 'inherit' });
  if (spawnResult.status !== 0) {
      process.kill(process.pid);
  }
  return spawnResult;
}

function runNpmCommand(args, cwd) {
    // execute command async
    return cp.spawn(npmCmd, args, { env: process.env, cwd, stdio: 'inherit' });
}

function runGitCommand(args, cwd) {
  // execute command sync
  const spawnResult = cp.spawnSync('git', args, { env: process.env, cwd, stdio: 'inherit' });
  if (spawnResult.status !== 0) {
      process.kill(process.pid);
  }
}

function forEachSubModule(callback) {   
    modulesRoots.forEach(function (modulesRoot) {
            fs.readdirSync(modulesRoot).forEach(function (mod) {
                const modPath = path.join(modulesRoot, mod);
                // ensure path has package.json
                if (!fs.existsSync(path.join(modPath, 'package.json'))) {
                    return;
                }
                callback(modPath);
            });
        });
}

function getProjectVersion() {
    const fileContent = fs.readFileSync(path.join(__dirname, "..", "pom.xml"));
    const pom = parser.parse(fileContent.toString());
    return pom.project.parent.version;
}

module.exports = {
    forEachSubModule,
    runNpmCommandSync,
    runNpmCommand,
    runGitCommand,
    getProjectVersion
}
