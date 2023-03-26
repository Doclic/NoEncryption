import argparse
import subprocess

def pre_main():
    arg_parser = argparse.ArgumentParser(
        prog="NoEncryption Version Bump Evaluator"
    )

    arg_parser.add_argument("-m", "--major", help="Major version (1 for true)", default=0)
    arg_parser.add_argument("-v", "--version", help="Current version")

    args = arg_parser.parse_args()

    global major, current_ver

    if int(args.major) == 1:
        major = True
    else:
        major = False

    current_ver = args.version

    main()

def main():
    new_ver = create_new_version(current_ver)

    add_env("NEW_VER", new_ver)
    print("New version set:", new_ver)

def create_new_version(version):
    version_0 = version.split(".")[0]
    version_1 = version.split(".")[1]

    if major:
        version_0 = int(version_0) + 1
        version_1 = 0
    else:
        version_1 = int(version_1) + 1

    return str(version_0) + "." + str(version_1)

def add_env(name, value):
    subprocess.run("echo \"{0}{1}\" >> $GITHUB_OUTPUT".format(name, value))

pre_main()
