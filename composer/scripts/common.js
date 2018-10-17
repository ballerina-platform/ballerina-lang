const fs = require('fs');
const join = require('path').join;
const resolve = require('path').resolve;
const cp = require('child_process');
const os = require('os');

// npm cmd based on OS
const npmCmd = os.platform().startsWith('win') ? 'npm.cmd' : 'npm';

// submodule root
const modulesRoot = resolve(__dirname, './../');

const submodules = [
    "theme",
    "api-designer",
    "documentation",
    "bbe",
    "diagram",
    "extended-language-client",
    "tracing",
//    "integration-tests",
    "distribution"	
]

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
    submodules.forEach(function (mod) {
        const modPath = join(modulesRoot, mod);
        // ensure path has package.json
        if (!fs.existsSync(join(modPath, 'package.json'))) {
            return;
        }
        callback(modPath);
    });
}

function getProjectVersion() {
    for(arg of process.argv.slice(2)) {
        if(arg.startsWith('--projectVersion=')) {
            return arg.split('=')[1] || "";
        }
    }
}

module.exports = {
    submodules,
    forEachSubModule,
    runNpmCommandSync,
    runNpmCommand,
    runGitCommand,
    modulesRoot,
    getProjectVersion
}
