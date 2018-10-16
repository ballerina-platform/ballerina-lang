const fs = require('fs');
const resolve = require('path').resolve;
const join = require('path').join;
const cp = require('child_process');
const os = require('os');

// submodule root
const modulesRoot = resolve(__dirname, './');

const modules = [
    "theme",
    "api-designer",
    "documentation",
    "bbe",
    "diagram",
    "extended-language-client",
    "tracing",
    "integration-tests",
    "distribution"	
];

function runNpmCommand(args, cwd) {
  // npm cmd based on OS
  const npmCmd = os.platform().startsWith('win') ? 'npm.cmd' : 'npm';

  // execute command sync
  const spawnResult = cp.spawnSync(npmCmd, args, { env: process.env, cwd, stdio: 'inherit' });
  if (spawnResult.status !== 0) {
      process.kill(process.pid);
  }
}

function runGitCommand(args, cwd) {
  // git cmd based on OS
  const gitCmd = os.platform().startsWith('win') ? 'git.cmd' : 'git';

  // execute command sync
  const spawnResult = cp.spawnSync(gitCmd, args, { env: process.env, cwd, stdio: 'inherit' });
  if (spawnResult.status !== 0) {
      process.kill(process.pid);
  }
}

function forEachSubModule(callback) {
    modules.forEach(function (mod) {
        const modPath = join(modulesRoot, mod);
        // ensure path has package.json
        if (!fs.existsSync(join(modPath, 'package.json'))) {
            return;
        }
        callback(modPath);
    });
}

forEachSubModule((modPath) => {
    runNpmCommand(["i"], modPath);
    runNpmCommand(["run", "build"], modPath);
    // Undo changes to package-lock files. These changes mostly happen
    // because of differences between npm versions
    runGitCommand(["checkout", "--", "package-lock.json"], modPath);
});
