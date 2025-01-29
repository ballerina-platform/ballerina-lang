import argparse
import json
import os
import subprocess
import sys
import requests

from colorama import Fore
from colorama import Style
from pathlib import Path
from configobj import ConfigObj

# Resources
TEST_IGNORE_MODULES_JSON = "https://raw.githubusercontent.com/ballerina-platform/" + \
                           "ballerina-release/master/dependabot/resources/full_build_ignore_modules.json"

# Repository names
BALLERINA_LANG_REPO_NAME = "ballerina-lang"
BALLERINA_DIST_REPO_NAME = "ballerina-distribution"
BALLERINAX = "ballerinax"

# File names
GRADLE_PROPERTIES = "gradle.properties"
INSTALLER_TEST_DIRECTORY = "ballerina-test-automation"
RELEASED_VERSION_PROPERTIES = "released_version.properties"
FAILED_MODULES_TEXT_FILE = "failed_modules.txt"

# Argument parser
parser = argparse.ArgumentParser(description="Full Build Pipeline")

# Mandatory arguments
parser.add_argument('path', help="Path to the directory where the standard library modules are (need to be) cloned")

# Optional arguments
parser.add_argument('--lang-branch', help="ballerina-lang branch to use for the builds (If it is not specified, " +
                                          "master branch will be used as the default branch)")
parser.add_argument('--lang-version', help="ballerina-lang version to use for the builds (If this argument passes, " +
                                           "--lang-branch argument won\'t work)")
parser.add_argument('--downstream-branch', help="Branch to build the downstream modules. (If the branch not found, " +
                                                "the default branch (or branch specified in ignore modules json " +
                                                "file) will be used)")
parser.add_argument('--update-stdlib-dependencies', action="store_true",
                    help="Replace all the standard library dependent versions with 'SNAPSHOT' versions. " +
                         "This is helpful to incrementally build libraries on top of a local change")
parser.add_argument('--build-released-versions', action="store_true",
                    help="Use released versions in " +
                         "https://github.com/ballerina-platform/ballerina-distribution/blob/master/gradle.properties" +
                         " according to the patch level (e.g.; 2201.4.x). Must use '--patch-level' flag to specify " +
                         "patch level")
parser.add_argument('--patch-level', help="Patch level for the build (e.g.; 2201.4.x)")
parser.add_argument('--build-level', help="Build only a specific level (e.g.; 1)")
parser.add_argument('--publish-to-local-central', action="store_true",
                    help="Publish all the modules to the local ballerina central repository")
parser.add_argument('--skip-tests', action="store_true", help="Skip tests in the builds")
parser.add_argument('--github-user', help="Github User to use for ballerina-lang repository")
parser.add_argument('--keep-local-changes', action="store_true",
                    help="Stop updating the repos from the origin. Keep the local changes")
parser.add_argument('--up-to-module', help="Build up to the specified module")
parser.add_argument('--from-module', help="Build from the specified module")
parser.add_argument('--test-module', help="Test the specified module (Only the dependency modules and dependent " +
                                          "modules will be built)")
parser.add_argument('--only-build-distribution', action="store_true",
                    help="If only the distribution should be built")
parser.add_argument('--skip-build-distribution', action="store_true",
                    help="If the distribution build should be skipped")
parser.add_argument('--additional-commands',
                    help="To provide a custom command to execute inside each repo. Provide this as a " +
                         "string. If not provided './gradlew clean build' will be used")
parser.add_argument('--continue-on-error', action="store_true",
                    help="Whether to continue the subsequent builds when a module build fails")
parser.add_argument('--remove-after-build', action="store_true",
                    help="Remove cloned module after the build")

github_user = 'ballerina-platform'
stdlib_modules_by_level = dict()
stdlib_versions = dict()
released_stdlib_versions = dict()

test_ignore_modules = []
build_ignore_modules = []
downstream_repo_branches = dict()
additional_arguments = dict()
released_version_data_file_url = None
distribution_level = None
module_list_json = "https://raw.githubusercontent.com/ballerina-platform/" + \
                   "ballerina-release/master/dependabot/resources/extensions.json"


def main():
    global module_list_json
    global stdlib_modules_by_level
    global test_ignore_modules
    global build_ignore_modules
    global downstream_repo_branches
    global additional_arguments
    global github_user
    global stdlib_versions
    global released_stdlib_versions
    global released_version_data_file_url

    failed_modules = []

    args = parser.parse_args()
    # Commands to be used to build downstream repositories (Except ballerina-distribution)
    commands = ["./gradlew", "clean", "build", "--stacktrace", "--scan", "--console=plain", "--no-daemon", "--continue"]

    lang_version = None
    downstream_branch = None
    patch_level = None

    skip_tests = False
    update_stdlib_dependencies = False
    build_released_versions = False
    keep_local_changes = False

    up_to_module = None
    from_module = None
    test_module = None
    build_level = None
    build_distribution = True
    only_build_distribution = False

    continue_on_error = False
    remove_after_build = False

    released_version_data_file_url = None

    print_block()

    if not args.path:
        print_error("Path to the directory where the standard library modules are (need to be) cloned has not been " +
                    "specified")
        exit(1)

    if not os.path.isdir(args.path):
        print_info("Provided root directory does not exist. Creating the directory and cloning the repositories")
        create_directory(args.path)

    # Change current directory to the specified directory
    os.chdir(args.path)

    if args.github_user:
        print_info("Using github user: " + args.github_user)
        github_user = args.github_user

    if args.keep_local_changes:
        print_info("Not updating the local repositories.")
        keep_local_changes = True
    else:
        print_info("Updating all the repositories. Any local change will be overridden")

    if args.skip_tests:
        print_info("Skipping tests for downstream modules")
        skip_tests = True
        commands.append("-x")
        commands.append("test")

    if args.lang_version:
        print_info("Using ballerina lang version: " + args.lang_version)
        lang_version = args.lang_version
    else:
        clone_repository(BALLERINA_LANG_REPO_NAME)
        os.chdir(BALLERINA_LANG_REPO_NAME)
        if args.lang_branch:
            print_info("Using ballerina lang branch: " + args.lang_branch)
            checkout_branch(args.lang_branch, keep_local_changes, "ballerinaLangVersion", build_released_versions)
        else:
            print_info("Using ballerina lang branch: master (default)")

        lang_version = get_version()
        print_info(f"Lang version: {lang_version}")
        lang_build_commands = ["./gradlew", "clean", "build", "--scan", "--stacktrace", "--console=plain",
                               "--no-daemon", "publishToMavenLocal"]

        # Temporary
        lang_build_commands.append("-x")
        lang_build_commands.append(":jballerina-debugger-integration-test:test")

        if skip_tests:
            lang_build_commands.append("-x")
            lang_build_commands.append("test")
        return_code = build_module(BALLERINA_LANG_REPO_NAME, lang_build_commands)
        os.chdir("..")

        if return_code != 0:
            exit_code = return_code
            failed_modules.append(BALLERINA_LANG_REPO_NAME)
            if not continue_on_error:
                write_failed_modules(failed_modules)
                exit(exit_code)

    if args.update_stdlib_dependencies:
        print_info("Using local SNAPSHOT builds for upper level stdlib dependencies")
        commands.append("publishToMavenLocal")
        update_stdlib_dependencies = True
    else:
        print_info("Using existing upper level stdlib versions for the builds")

    if args.patch_level:
        print_info(f"Using patch level: {args.patch_level}")
        patch_level = args.patch_level
        module_list_json = "https://raw.githubusercontent.com/ballerina-platform/ballerina-release/" + \
                           args.patch_level + "/dependabot/resources/extensions.json"

    if args.downstream_branch:
        print_info(f"Using downstream branch: {args.downstream_branch}")
        downstream_branch = args.downstream_branch

    if args.build_released_versions:
        print_info("Using released versions for build in " +
                   f"https://github.com/ballerina-platform/ballerina-distribution/blob/{patch_level}/gradle.properties")
        build_released_versions = True

    if args.skip_build_distribution:
        print_info("Skipping ballerina-distribution build")
        build_distribution = False

    if args.only_build_distribution:
        only_build_distribution = True

    if args.publish_to_local_central:
        print_info("Pushing all the modules to local ballerina central repository")
        commands.append("-PpublishToLocalCentral=true")

    if args.additional_commands:
        commands = commands + list(filter(None, map(lambda command: command.strip(),
                                                    args.additional_commands.split(" "))))
        print_info(f'Using the command: "{" ".join(commands)}"')
    else:
        print_info(f'Using the command: "{" ".join(commands)}"')

    if args.continue_on_error:
        print_warn("Continuing the build even if a module build fails. However it will be stopped at the failing " +
                   "module level if '--update-stdlib-dependencies' is being used.")
        continue_on_error = True

    if args.up_to_module:
        print_info("Building up to the module: " + args.up_to_module)
        up_to_module = args.up_to_module

    if args.from_module:
        print_info("Building from the module: " + args.from_module)
        from_module = args.from_module

    if args.build_level:
        print_info("Building the level: " + args.build_level)
        build_level = args.build_level

    if args.test_module:
        print_info("Testing module: " + args.test_module)
        test_module = args.test_module
        print_warn("'--test-module' flag will override '--up-to-module' & '--from-module' flags. It will skip the " +
                   "tests for dependency modules and dependent modules if '--update-stdlib-dependencies' flag " +
                   "is being used")

    if args.remove_after_build:
        print_info("Modules will be removed after the build")
        remove_after_build = True

    # Get released stdlib versions from ballerina-distribution/gradle.properties
    if build_released_versions:
        if patch_level:
            released_version_data_file_url = "https://raw.githubusercontent.com/ballerina-platform/" + \
                                             f"ballerina-distribution/{patch_level}/gradle.properties"
            read_released_stdlib_versions(released_version_data_file_url)
        else:
            print_error("Patch Level must be defined using '--patch-level' flag if you are using " +
                        "'--build-released-versions' flag")
            exit(1)

    read_stdlib_data(test_module)
    read_ignore_modules(patch_level)

    start_build = False if from_module else True
    exit_code = 0
    if only_build_distribution:
        start_build = False

    module_levels = list(stdlib_modules_by_level.keys())
    module_levels.sort()
    for level in module_levels:
        if build_level:
            if int(build_level) == level:
                start_build = True
            else:
                start_build = False
        for module in stdlib_modules_by_level[level]:
            module_name = module['name']
            module_version_key = module['version_key']

            if from_module == module_name:
                start_build = True

            if module_name in build_ignore_modules:
                print_separation_block()
                print_info(print_info("Skipping: " + module_name))
            elif build_released_versions and module_version_key not in released_stdlib_versions:
                print_separation_block()
                print_info(print_info("Skipping: " + module_name))
            elif start_build or update_stdlib_dependencies:
                print_separation_block()
                clone_repository(module_name)

                os.chdir(module_name)
                process_module(module_name, module_version_key, lang_version, patch_level, build_released_versions,
                               update_stdlib_dependencies, keep_local_changes, downstream_branch)

                if start_build:
                    build_commands = commands.copy()
                    if not skip_tests and test_module and test_module != module_name:
                        build_commands.append("-x")
                        build_commands.append("test")
                    if module_name in additional_arguments:
                        build_commands += additional_arguments[module_name]

                    return_code = build_module(module_name, build_commands)

                    if return_code != 0:
                        exit_code = return_code
                        failed_modules.append(module_name)
                        if not continue_on_error:
                            write_failed_modules(failed_modules)
                            exit(exit_code)

                os.chdir("..")

                if remove_after_build:
                    delete_module(module_name)

            if up_to_module == module_name:
                start_build = False

        if exit_code != 0:
            write_failed_modules(failed_modules)
            exit(exit_code)

        clean_docker_container()

    start_build = True
    if build_level:
        if int(build_level) == distribution_level:
            start_build = True
        else:
            start_build = False
    if test_module and test_module != BALLERINA_DIST_REPO_NAME:
        start_build = False

    if build_distribution and start_build:
        print_separation_block()
        clone_repository(BALLERINA_DIST_REPO_NAME)

        os.chdir(BALLERINA_DIST_REPO_NAME)
        process_module(BALLERINA_DIST_REPO_NAME, None, lang_version, patch_level, build_released_versions,
                       update_stdlib_dependencies, keep_local_changes, downstream_branch)
        dist_build_commands = commands.copy()

        # Temporary
        dist_build_commands.append("-x")
        dist_build_commands.append(":project-api-tests:test")
        dist_build_commands.append("-x")
        dist_build_commands.append(":ballerina:testExamples")

        if not skip_tests and test_module and test_module != BALLERINA_DIST_REPO_NAME:
            dist_build_commands.append("-x")
            dist_build_commands.append("test")

        return_code = build_module(BALLERINA_DIST_REPO_NAME, dist_build_commands)
        os.chdir("..")

        if return_code != 0:
            exit_code = return_code
            failed_modules.append(BALLERINA_DIST_REPO_NAME)
            write_failed_modules(failed_modules)
            exit(exit_code)

    # update_installer_versions(lang_version)


def process_module(module_name, module_version_key, lang_version, patch_level, use_released_versions,
                   update_stdlib_dependencies, keep_local_changes, downstream_branch):
    global stdlib_versions

    print_block()
    print_info("Processing: " + module_name)

    module_branch = "master"
    if downstream_branch:
        module_branch = downstream_branch
        print_info(f"Using given downstream branch {module_branch}")

    if module_name != BALLERINA_DIST_REPO_NAME:
        if module_name in downstream_repo_branches:
            module_branch = downstream_repo_branches[module_name]
            print_info(f"Using defined branch {module_branch} in {TEST_IGNORE_MODULES_JSON}")
            checkout_branch(module_branch, keep_local_changes, module_version_key, False)
        else:
            checkout_branch(module_branch, keep_local_changes, module_version_key, use_released_versions)
    elif patch_level:
        module_branch = patch_level
        print_info(f"Using patch branch {module_branch} for {BALLERINA_DIST_REPO_NAME}")
        checkout_branch(module_branch, keep_local_changes, module_version_key, False)
    else:
        checkout_branch(module_branch, keep_local_changes, module_version_key, use_released_versions)

    module_version = get_version()
    print_info(f"Module {module_name} version: {module_version}")
    stdlib_versions[module_version_key] = module_version

    update_lang_version(lang_version)

    if update_stdlib_dependencies:
        update_stdlibs_version()
        remove_dependency_files(module_name)


def build_module(module_name, commands):
    print_block()
    print_info(f"Building Module: {module_name}")
    process = subprocess.run(commands)

    return process.returncode


def remove_dependency_files(module_name):
    commands = ["find", ".", "-name", "Dependencies.toml", "-delete"]
    if Path("./ballerina/Dependencies.toml").is_file():
        print_info("Removing Dependencies.toml files in the ballerina directory")
        os.chdir("ballerina")
        subprocess.run(commands)
        os.chdir("..")

    elif module_name == "module-ballerinai-transaction" and \
            Path("./transaction-ballerina/Dependencies.toml").is_file():
        print_info("Removing Dependencies.toml files in the transaction-ballerina directory")
        os.chdir("transaction-ballerina")
        subprocess.run(commands)
        os.chdir("..")

    if Path("./ballerina-tests/Dependencies.toml").is_file():
        print_info("Removing Dependencies.toml files in the ballerina-tests directory")
        os.chdir("ballerina-tests")
        subprocess.run(commands)
        os.chdir("..")


def delete_module(module_name):
    process = subprocess.run(["rm", "-rf", f"./{module_name}"])
    if process.returncode != 0:
        exit(process.returncode)


def update_lang_version(lang_version):
    config = ConfigObj(GRADLE_PROPERTIES)
    config['ballerinaLangVersion'] = lang_version
    config.write()
    print_info(f"Updating lang version: {config['ballerinaLangVersion']}")


def update_stdlibs_version():
    config = ConfigObj(GRADLE_PROPERTIES)
    properties = config.keys()
    for version_key in stdlib_versions:
        if version_key in properties:
            config[version_key] = stdlib_versions[version_key]
            print_info(f"Updating {version_key}: {stdlib_versions[version_key]}")
    config.write()


def clone_repository(module_name):
    print_info(f"Cloning Module: {module_name}")
    if module_name == "ballerina-lang":
        repo_path = f"https://www.github.com/{github_user}/{module_name}.git"
    else:
        repo_path = f"https://www.github.com/ballerina-platform/{module_name}.git"
    subprocess.run(["git", "clone", repo_path])


def get_version():
    config = ConfigObj(GRADLE_PROPERTIES)
    version = config['version']

    return version


def read_released_stdlib_versions(url):
    global released_stdlib_versions

    try:
        response = requests.get(url)
        if response.status_code == 200:
            open(RELEASED_VERSION_PROPERTIES, "wb").write(response.content)

            config = ConfigObj(RELEASED_VERSION_PROPERTIES)
            for property in config.keys():
                released_stdlib_versions[property] = config[property]
        else:
            print_error(f"Failed to access released version data from {url}")
            exit(1)
    except json.decoder.JSONDecodeError:
        print_error(f"Failed to access released version data from {url}")
        exit(1)


# def update_installer_versions(lang_version):
#     print_info(f"Updating installer test versions..")
#
#     ballerina_lang_configs = ConfigObj(BALLERINA_LANG_REPO_NAME + "/" + GRADLE_PROPERTIES)
#     ballerina_distribution_configs = ConfigObj(BALLERINA_DIST_REPO_NAME + "/" + GRADLE_PROPERTIES)
#     installer_test_configs = ConfigObj(BALLERINA_DIST_REPO_NAME + "/" + INSTALLER_TEST_DIRECTORY + "/" +
#                                        GRADLE_PROPERTIES)
#
#     display_text = lang_version.split("-")[0]
#     swan_lake_latest_version = "swan-lake-" + display_text
#     spec_version = ballerina_lang_configs['specVersion']
#     update_tool_version = ballerina_distribution_configs['ballerinaCommandVersion']
#
#     installer_test_configs['swan-lake-latest-version'] = swan_lake_latest_version
#     installer_test_configs['swan-lake-latest-version-display-text'] = display_text
#     installer_test_configs['swan-lake-latest-spec-version'] = spec_version
#     installer_test_configs['latest-tool-version'] = update_tool_version
#     installer_test_configs['swan-lake-latest-tool-version'] = update_tool_version
#     installer_test_configs.write()


def checkout_branch(branch, keep_local_changes, module_key, build_released_versions):
    try:
        if build_released_versions:
            released_version = released_stdlib_versions[module_key]
            if len(released_version.split("-")) > 1:
                updated_commit_id = released_version.split("-")[-1]
                process = subprocess.run(["git", "checkout", "-b", "full-build", updated_commit_id])
                print_info(f"Using commit ID {updated_commit_id} to create a new branch")
            else:
                process = subprocess.run(["git", "checkout", f"v{released_version}"])
                print_info(f"Using released version tag v{released_version} in {released_version_data_file_url}")
        else:
            process = subprocess.run(["git", "checkout", branch])
            print_info(f"Using branch {branch}")

        if process.returncode != 0:
            print_warn(f"Failed to checkout branch {branch}. Default branch will be used.")
        # if not keep_local_changes:
        #     subprocess.run(["git", "reset", "--hard", "origin/" + branch])
        #     subprocess.run(["git", "pull", "origin", branch])
    except Exception as e:
        print_warn("Failed to Sync the Default Branch: " + str(e))


def create_directory(directory_name):
    Path(directory_name).mkdir(parents=True, exist_ok=True)


def read_stdlib_data(test_module):
    global stdlib_modules_by_level

    try:
        response = requests.get(module_list_json)
        if response.status_code == 200:
            stdlib_modules_data = json.loads(response.text)
            if test_module:
                read_data_for_module_testing(stdlib_modules_data, test_module)
            else:
                read_data_for_fbp(stdlib_modules_data)
        else:
            print_error(f"Failed to access standard library dependency data from {module_list_json}")
            exit(1)
    except json.decoder.JSONDecodeError:
        print_error("Failed to load standard library dependency data")
        exit(1)


def read_data_for_fbp(stdlib_modules_data):
    global stdlib_modules_by_level
    global distribution_level

    for module in stdlib_modules_data['standard_library']:
        name = module['name']
        level = module['level']
        version_key = module['version_key']
        if name != BALLERINA_DIST_REPO_NAME and BALLERINAX not in name:
            stdlib_modules_by_level[level] = stdlib_modules_by_level.get(level, []) + \
                                             [{"name": name, "version_key": version_key}]
        else:
            distribution_level = level


def read_data_for_module_testing(stdlib_modules_data, test_module_name):
    global stdlib_modules_by_level

    standard_library_data = dict()
    module_dependencies = dict()
    for module in stdlib_modules_data['standard_library']:
        module_name = module['name']
        standard_library_data[module_name] = module
        dependents = module['dependents']
        for dependent in dependents:
            module_dependencies[dependent] = module_dependencies.get(dependent, []) + [module_name]

    if test_module_name not in standard_library_data.keys():
        print_error(f"Desired module {test_module_name} for testing was not found in {module_list_json}")
        exit(1)

    module_list = {test_module_name}
    while module_list:
        current_module_name = module_list.pop()
        level = standard_library_data[current_module_name]['level']
        version_key = standard_library_data[current_module_name]['version_key']
        if level in stdlib_modules_by_level.keys():
            repeated = False
            for module in stdlib_modules_by_level[level]:
                if module["name"] == current_module_name:
                    repeated = True
                    break
            if not repeated:
                stdlib_modules_by_level[level] = stdlib_modules_by_level.get(level, []) + \
                                                 [{"name": current_module_name, "version_key": version_key}]
        else:
            stdlib_modules_by_level[level] = [{"name": current_module_name, "version_key": version_key}]

        if current_module_name in module_dependencies.keys():
            dependencies = set(module_dependencies[current_module_name])
            module_list = module_list.union(dependencies)

    stdlib_levels = list(stdlib_modules_by_level.keys())
    stdlib_levels.sort()
    print_info("Following modules will be built with the pipeline")
    for level in stdlib_levels:
        print_info("Build Level: " + str(level))
        module_names = [module['name'] for module in stdlib_modules_by_level[level]]
        print_info("Modules: " + ", ".join(module_names))
    print_info("Testing Module: " + test_module_name)


def read_ignore_modules(patch_level):
    global test_ignore_modules
    global build_ignore_modules
    global downstream_repo_branches
    global additional_arguments

    try:
        response = requests.get(TEST_IGNORE_MODULES_JSON)
        if response.status_code == 200:
            data = json.loads(response.text)
            if patch_level:
                test_ignore_modules = data[patch_level]['test-ignore-modules']
                build_ignore_modules = data[patch_level]['build-ignore-modules']
                downstream_repo_branches = data[patch_level]['downstream-repo-branches']
                additional_arguments = data[patch_level]['additional-arguments']
            else:
                test_ignore_modules = data['master']['test-ignore-modules']
                build_ignore_modules = data['master']['build-ignore-modules']
                downstream_repo_branches = data['master']['downstream-repo-branches']
                additional_arguments = data['master']['additional-arguments']
        else:
            print_error(f"Failed to load test ignore modules from {TEST_IGNORE_MODULES_JSON}")
            exit(1)
    except json.decoder.JSONDecodeError:
        print_error(f"Failed to load test ignore modules from {TEST_IGNORE_MODULES_JSON}")
        exit(1)


def clean_docker_container():
    print_block()
    print_info(f"Cleaning used docker containers")
    commands = ["docker", "system", "prune", "-af"]
    process = subprocess.run(commands)

    return process.returncode

def write_failed_modules(failed_module_names):
    with open(FAILED_MODULES_TEXT_FILE, "w") as file:
        for module_name in failed_module_names:
            file.write(module_name + "\n")
            print_error(f"Build failed for {module_name}")
        file.close()


def print_info(message):
    print(f'{Fore.GREEN}[INFO] {message}{Style.RESET_ALL}')


def print_error(message):
    print(f'{Fore.RED}[ERROR] {message}{Style.RESET_ALL}')
    sys.exit(1)


def print_warn(message):
    print(f'{Fore.YELLOW}[WARN] {message}{Style.RESET_ALL}')


def print_block():
    print(f'{Fore.GREEN}############################################################################################'
          f'{Style.RESET_ALL}')


def print_separation_block():
    print(f'{Fore.GREEN}############################################################################################'
          f'{Style.RESET_ALL}')
    print(f'{Fore.GREEN}############################################################################################'
          f'{Style.RESET_ALL}')


main()
