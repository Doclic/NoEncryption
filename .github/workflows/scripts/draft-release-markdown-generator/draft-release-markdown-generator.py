import argparse
import os
import os.path as path
from json import loads as jsonload
from json import load as jsonloadfile
from os import sep
from sys import exit
from shutil import rmtree, copyfile, copytree, make_archive
from urllib.request import urlretrieve, urlopen
from zipfile import ZipFile

class FileUtils:
    def zip_folder(source, destination):
        make_archive(
            destination.replace(".zip", ""),
            "zip", 
            source
        )
        
    def extract_zip(zip, destination):
        with ZipFile(zip, 'r') as zip:
            zip.extractall(destination)

    def copy_file(file, destination):
        copyfile(file, destination)

    def copy_files(folder, destination):
        for file in FileUtils.list_files(folder):
            copyfile(file, path.join(destination, file.split(sep)[-1]))

    def write_file(file, contents):
        with open(file, 'w', encoding="utf-8") as f:
            f.write(contents)

    def list_files(folder, map=True):
        final_list = []

        for file in os.listdir(folder):
            if map:
                file = path.join(folder, file)

            final_list.append(file)

        return final_list
    
    def clean_folder(folder):
        try:
            rmtree(folder)
        except:
            ""
            
        FileUtils.verify_folder(folder)

    def verify_folder(folder):
        os.makedirs(folder, exist_ok=True)
    
    def verify_json(file):
        try:
            with open(file, 'r') as f:
                jsonloadfile(f)

            return True
        except:
            return False
        
class WebHandler:
    def get_web_contents(url):
        return urlopen(url)

    def download_file(url, destination):
        return urlretrieve(url, destination)

def calculate_changes(tag, previous, changes, meta, file_order, module_order, artifacts_detail):
    final_str = ""

    final_str += "# NoEncryption " + tag + "\n"
    final_str += "\n"
    final_str += "## Main Changes" + "\n"
    final_str += "\n"
    final_str += "- " + "\n"
    final_str += "\n"
    final_str += "## Smaller Changes" + "\n"
    final_str += "\n"
    final_str += "- " + "\n"
    final_str += "\n"
    final_str += "## Detailed Change List" + "\n"
    final_str += "\n"
    final_str += "- " + f"https://github.com/Doclic/NoEncryption/compare/{previous}...{tag}" + "\n"
    final_str += "\n"
    final_str += "### Notable Modifications" + "\n"
    final_str += "\n"

    metas = {}
    meta_files = {}
    json = {}

    for change in changes:
        if not changes.get(change).get("status") == "REMOVED":
            metas[change] = meta.get(change)

    for current in file_order:
        c_change = changes.get(current)

        if not c_change.get("status") == "REMOVED":
            match c_change.get("status"):
                case "NONE":
                    with open(c_change.get("paths").get("before"), 'r') as f:
                        before_json = jsonloadfile(f)
                    with open(c_change.get("paths").get("after"), 'r') as f:
                        after_json = jsonloadfile(f)
                case "ADDED":
                    before_json = None
                    with open(c_change.get("paths").get("after"), 'r') as f:
                        after_json = jsonloadfile(f)
            
            json[current] = {
                "before": before_json,
                "after": after_json
            }

            module_changes = {}

            if before_json.keys() != after_json.keys():
                for module in before_json:
                    if module not in after_json:
                        module_changes[module] = "REMOVED"

                for module in after_json:
                    if module not in before_json:
                        module_changes[module] = "ADDED"
            
            for module in before_json:
                if module not in module_changes:
                    module_changes[module] = "NONE"
                    
            for module in after_json:
                if module not in module_changes:
                    module_changes[module] = "NONE"

            meta_files[current] = module_changes

    for current in file_order:
        meta = metas.get(current)

        before_json = json.get(current).get("before")
        after_json = json.get(current).get("after")

        modules = []

        for module in meta_files.get(current):
            if meta_files.get(current).get(module) != "REMOVED":
                modules.append(module)

        changed_modules = []

        for module in modules:
            before_keys = before_json.get(module).keys()
            after_keys = after_json.get(module).keys()

            before_keys = sorted(before_keys)
            after_keys = sorted(after_keys)

            if before_keys != after_keys:
                changed_modules.append(module)

        module_changes = {}

        for module in modules:
            if module in changed_modules:
                changed = False
                changed_sets = {}
                for key in before_json.get(module):
                    if key not in after_json.get(module):
                        changed = True
                        changed_sets[key] = "REMOVED"
                        
                for key in after_json.get(module):
                    if key not in before_json.get(module):
                        changed = True
                        changed_sets[key] = "ADDED"

                if changed:
                    module_changes[module] = {}

                    for set in changed_sets:
                        module_changes[module][set] = changed_sets.get(set)

        final_str += "<details>" + "\n"
        final_str += "<summary>" + meta.get("display_names").get("group_display_name") + "</summary>" + "\n"
        final_str += "\n"
        final_str += "> " + "<details>" + "\n"
        final_str += "> " + "<summary>" + meta.get("display_names").get("added_display_name") + "</summary>" + "\n"
        final_str += "> " + "\n"

        added = False

        if module in changed_modules:
            for change in module_changes.get(module):
                if module_changes.get(module).get(change) == "ADDED":
                    added = True

        if len(changed_modules) == 0 and len(module_changes) == 0 or not added:
            final_str += "> > " + "No changed detected." + "\n"
        else:
            for module in module_order:
                final_str += "> > " + "<details>" + "\n"
                final_str += "> > " + "<summary>" + module + "</summary>" + "\n"
                final_str += "> > " + "\n"

                added_values = []

                if module in changed_modules:
                    for change in module_changes.get(module):
                        if module_changes.get(module).get(change) == "ADDED":
                            added_values.append(change)

                    if len(added_values) == 0:
                        final_str += "> > " + "No changed detected." + "\n"

                    for value in added_values:
                        final_str += "> > > " + "- `" + after_json.get(module).get(value).get(meta.get("values").get("added").get("header")) + "`" + "\n"
                        
                        for line in meta.get("values").get("added").get("values"):
                            match meta.get("values").get("added").get("values").get(line).get("type"):
                                case "string":
                                    new_values = []

                                    for format_val in meta.get("values").get("added").get("values").get(line).get("formatting"):
                                        new_values.append(after_json.get(module).get(value).get(format_val))

                                    final_str += "> > > " + "  - " + meta.get("values").get("added").get("values").get(line).get("value").format(*new_values) + "\n"
                                case "list":
                                    list_values = after_json.get(module).get(value).get(meta.get("values").get("added").get("values").get(line).get("list_value"))

                                    final_str += "> > > " + "  - " + meta.get("values").get("added").get("values").get(line).get("value") + "\n"
                                    
                                    if len(list_values) == 0:
                                        final_str += "> > > " + "    - " + meta.get("values").get("added").get("values").get(line).get("empty_value") + "\n"
                                    else:
                                        for list_val in list_values:
                                            final_str += "> > > " + "    - " + meta.get("values").get("added").get("values").get(line).get("individual_value").format(list_val) + "\n"
                            
                        final_str += "> > " + "\n"
                else:
                    final_str += "> > > " + "No changed detected." + "\n"
                    final_str += "> > " + "\n"
                
                final_str += "> > " + "</details>" + "\n"

        final_str += "> " + "\n"
        final_str += "> " + "</details>" + "\n"
        final_str += "\n"
        final_str += "> " + "<details>" + "\n"
        final_str += "> " + "<summary>" + meta.get("display_names").get("removed_display_name") + "</summary>" + "\n"
        final_str += "> " + "\n"

        removed = False

        if module in changed_modules:
            for change in module_changes.get(module):
                if module_changes.get(module).get(change) == "REMOVED":
                    removed = True

        if len(changed_modules) == 0 and len(module_changes) == 0 or not removed:
            final_str += "> > " + "No changed detected." + "\n"
        else:
            for module in module_order:
                final_str += "> > " + "<details>" + "\n"
                final_str += "> > " + "<summary>" + module + "</summary>" + "\n"
                final_str += "> > " + "\n"

                removed_values = []

                if module in changed_modules:
                    for change in module_changes.get(module):
                        if module_changes.get(module).get(change) == "REMOVED":
                            removed_values.append(change)

                    if len(removed_values) == 0:
                        final_str += "> > " + "No changed detected." + "\n"

                    for value in removed_values:
                        final_str += "> > > " + "- `" + before_json.get(module).get(value).get(meta.get("values").get("added").get("header")) + "`" + "\n"
                        
                        for line in meta.get("values").get("removed").get("values"):
                            match meta.get("values").get("removed").get("values").get(line).get("type"):
                                case "string":
                                    removed_values = []

                                    for format_val in meta.get("values").get("removed").get("values").get(line).get("formatting"):
                                        removed_values.append(before_json.get(module).get(value).get(format_val))

                                    final_str += "> > > " + "  - " + meta.get("values").get("removed").get("values").get(line).get("value").format(*removed_values) + "\n"
                                case "list":
                                    list_values = before_json.get(module).get(value).get(meta.get("values").get("removed").get("values").get(line).get("list_value"))

                                    final_str += "> > > " + "  - " + meta.get("values").get("removed").get("values").get(line).get("value") + "\n"
                                    
                                    if len(list_values) == 0:
                                        final_str += "> > > " + "    - " + meta.get("values").get("removed").get("values").get(line).get("empty_value") + "\n"
                                    else:
                                        for list_val in list_values:
                                            final_str += "> > > " + "    - " + meta.get("values").get("removed").get("values").get(line).get("individual_value").format(list_val) + "\n"
                            
                        final_str += "> > " + "\n"
                else:
                    final_str += "> > > " + "No changed detected." + "\n"
                    final_str += "> > " + "\n"
                
                final_str += "> > " + "</details>" + "\n"

        final_str += "> " + "\n"
        final_str += "> " + "</details>" + "\n"
        final_str += "\n"
        final_str += "</details>" + "\n"
        final_str += "\n"

    final_str += "## Choosing a JAR" + "\n"
    final_str += "\n"
    final_str += "There are multiple NoEncryption JARs available for download. Make sure you are using the right JAR that supports your server version. Artifacts titled <code>Source code</code> do not contain ready-to-use JAR files." + "\n"
    final_str += "\n"
    final_str += "<details>" + "\n"
    final_str += "<summary>File Version Table</summary>" + "\n"
    final_str += "\n"
    final_str += "> " + "<table>" + "\n"
    final_str += "> " + "<tr>" + "\n"
    final_str += "> " + "<th style=\"text-align:center\">JAR File</th>" + "\n"
    final_str += "> " + "<th style=\"text-align:center\">Min</th>" + "\n"
    final_str += "> " + "<th style=\"text-align:center\">Max</th>" + "\n"
    final_str += "> " + "</tr>" + "\n"

    with open(artifacts_detail, 'r') as artifact_json:
        json = jsonloadfile(artifact_json)

    for artifact in json.keys():
        final_str += "> " + "<tr>" + "\n"
        final_str += "> " + "<td style=\"text-align:center\"><code>" + artifact.format(tag) + "</code></td>" + "\n"
        final_str += "> " + "<td style=\"text-align:center\">" + json.get(artifact).get("version-min") + "</td>" + "\n"
        final_str += "> " + "<td style=\"text-align:center\">" + json.get(artifact).get("version-max") + "</td>" + "\n"
        final_str += "> " + "</tr>" + "\n"

    final_str += "> " + "</table>" + "\n"
    final_str += "\n"
    final_str += "</details>" + "\n"

    return final_str

def pre_main():
    arg_parser = argparse.ArgumentParser()

    arg_parser.add_argument("-n", "--new-meta", help="New meta location", default="../../../meta")
    arg_parser.add_argument("-r", "--repo", help="Repository to pull data from. Format: OWNER/REPO", default="Doclic/NoEncryption")
    arg_parser.add_argument("-t", "--tag", help="Release tag")

    args = arg_parser.parse_args()

    global repository, new_meta, tag

    repository = args.repo
    new_meta = args.new_meta
    tag = args.tag

    main()

def main():
    cwd = os.getcwd()

    temp_folder = path.join(cwd, r'temp' + sep)
    fallback_main_folder = path.join(cwd, r'fallback')
    fallback_zip_file_folder = path.join(fallback_main_folder, r'zipball')
    zipball_folder = path.join(temp_folder, r'previous')
    zipball_zip = zipball_folder + ".zip"
    latest_release_folder = path.join(temp_folder, r'latest_release')
    latest_release_meta_folder = path.join(latest_release_folder, r'.github' + sep + r'meta')
    latest_release_meta_files_folder = path.join(temp_folder, r'latest_meta')
    fallback_meta_folder = path.join(fallback_zip_file_folder, r'NoEncryption' + sep + r'.github' + sep + r'meta')
    new_meta_original_folder = path.realpath(new_meta)
    new_meta_files_folder = path.join(temp_folder, r'new_meta')
    artifacts_detail_file = path.join(new_meta_files_folder, r'artifacts.json')
    markdown_output_file = path.join(cwd, r'output.md')

    latest_release_location = f"https://api.github.com/repos/{repository}/releases?per_page=1"
    
    fallback_latest_release_location = False
    fallback_meta = False
    fallback_new_meta = False

    # Properties for meta files
    #   When adding modules, add to all meta files, and fallback files.
    #   When adding meta files, add to main meta folder and fallback folder

    # module_order      Order to process modules in
    # file_order        Order to process meta files in
    # file_meta         Values for rendering markdown output
    module_order = [
        "1.19",
        "1.19.1",
        "1.19.2",
        "1.19.3",
        "1.19.4",
        "Reflection"
    ]
    file_order = [
        "commands.json",
        "permissions.json"
    ]
    file_meta = {
        # Main keys     Meta file names
        "commands.json": {
            # display_names     Values to use for rendering categories in markdown
            # values            Values to use for rendering added/removed data in meta files
            "display_names": {
                # group_display_name        Meta file group display name
                # added_display_name        Added meta value group display name
                # removed_display_name      Removed meta value group display name  
                "group_display_name": "💬 Commands",
                "added_display_name": "➕ Added Commands",
                "removed_display_name": "➖ Removed Commands",
            },
            "values": {
                # added       Values for added meta values
                # removed     Values for removed meta values
                "added": {
                    # header                        JSON key to use for list header
                    # values                        List of values for each list
                    # 
                    #   values.X.type: string
                    # values.X.value                Unformatted value. Use {0}, {1}, etc.
                    # values.X.formatting           Formatting for `value`. Uses JSON key values
                    # 
                    #   values.X.type: list
                    # values.X.value                List title
                    # values.X.list_value           Values for list. Uses JSON key for a string list (["a", "b", etc.])
                    # values.X.individual_value     Formatting for each list value. Must have one and only one format tag ({0})
                    # values.X.empty_value          Value to use when JSON key string list is empty
                    "header": "name",
                    "values": {
                        0: {
                            "type": "string",
                            "value": "{0}",
                            "formatting": ["description"]
                        },
                        1: {
                            "type": "string",
                            "value": "**Permission**: `{0}`",
                            "formatting": ["permission"]
                        }
                    }
                },
                "removed": {
                    "header": "name",
                    "values": {}
                }
            }
        },
        "permissions.json": {
            "display_names": {
                "group_display_name": "👷 Permissions",
                "added_display_name": "➕ Added Permissions",
                "removed_display_name": "➖ Removed Permissions",
            },
            "values": {
                "added": {
                    "header": "name",
                    "values": {
                        0: {
                            "type": "string",
                            "value": "{0}",
                            "formatting": ["description"]
                        },
                        1: {
                            "type": "string",
                            "value": "**Default**: `{0}`",
                            "formatting": ["default"]
                        },
                        2: {
                            "type": "list",
                            "value": "**Children**:",
                            "list_value": "children",
                            "individual_value": "`{0}`",
                            "empty_value": "None."
                        }
                    }
                },
                "removed": {
                    "header": "name",
                    "values": {}
                }
            }
        }
    }
    
    print("Cleaning up possible previous runs...")
    FileUtils.clean_folder(temp_folder)

    print("Fetching latest release meta from", latest_release_location)
    try:
        latest_release_json = jsonload(
            WebHandler.get_web_contents(latest_release_location).read()
        )[0]

        latest_release_zipball_location = latest_release_json["zipball_url"]
        previous_tag = latest_release_json["tag_name"]
    except:
        print("Could not connect to", latest_release_location)
        print("Using fallback values")

        fallback_latest_release_location = True
        previous_tag = tag

    if fallback_latest_release_location:
        print("ZIP download skipped. Using local fallback")

        FileUtils.zip_folder(
            fallback_zip_file_folder,
            zipball_folder
        )
    else:
        print("Downloading previous release ZIP from", latest_release_zipball_location)

        try:
            WebHandler.download_file(latest_release_zipball_location, zipball_zip)
        except:
            print("Could not connect to", latest_release_zipball_location)
            print("Using local fallback")

            FileUtils.zip_folder(
                fallback_zip_file_folder,
                zipball_folder
            )

    print("Extracting ZIP")
    try:
        FileUtils.extract_zip(
            zipball_zip, 
            zipball_folder
        )
    except Exception as ex:
        print("Could not extract ZIP file")
        raise ex

    print("Moving files in ZIP up one layer")
    try:
        for dir in FileUtils.list_files(zipball_folder):
            copytree(dir, latest_release_folder)
            rmtree(dir)
    except Exception as ex:
        print("Could not modify extracted ZIP")
        raise ex
    
    print("Cherry picking meta files")
    try:
        for latest_meta_file in FileUtils.list_files(latest_release_meta_folder, map=False):
            output_file = path.join(latest_release_meta_files_folder, latest_meta_file)
            latest_meta_file = path.join(latest_release_meta_folder, latest_meta_file)

            FileUtils.copy_file(latest_meta_file, output_file)
    except FileNotFoundError:
        print("No meta folder detected. Using fallback meta template")

        fallback_meta = True
    except Exception as ex:
        print("Unable to cherry pick meta files")
        raise ex

    if fallback_meta:
        print("Copying files from fallback folder")
        
        FileUtils.verify_folder(latest_release_meta_files_folder)

        try:
            FileUtils.copy_files(fallback_meta_folder, latest_release_meta_files_folder)
        except Exception as ex:
            print("Unable to copy files from fallback folder")
            raise ex
    else:
        print("Copying files from latest release meta folder")

        try:
            FileUtils.copy_files(latest_release_meta_folder, latest_release_meta_files_folder)
        except Exception as ex:
            print("Unable to copy files from latest release folder")
            raise ex

    print("Copying latest meta from", new_meta_original_folder)
    try:
        FileUtils.verify_folder(new_meta_files_folder)
        FileUtils.copy_files(new_meta_original_folder, new_meta_files_folder)
    except: 
        print("Unable to copy new meta folder. Using fallback meta")

        fallback_new_meta = True

        try:
            FileUtils.verify_folder(new_meta_files_folder)
            FileUtils.copy_files(fallback_meta_folder, new_meta_files_folder)
        except Exception as ex:
            print("Unable to copy meta from fallback meta")
            raise ex

    print("Validating JSON files")
    json_results_latest = {}
    
    for file in FileUtils.list_files(latest_release_meta_files_folder):
        json_results_latest[file] = {
            "name": file.split(sep)[-1],
            "result": FileUtils.verify_json(file)
        }

    for result in json_results_latest:
        if not json_results_latest.get(result).get("result"):
            if fallback_meta:
                print("Unable to load latest fallback meta", json_results_latest.get(result).get("name"))
            else:
                print("Unable to load latest meta", json_results_latest.get(result).get("name"))
            exit()

    json_results_new = {}
    
    for file in FileUtils.list_files(new_meta_files_folder):
        json_results_new[file] = {
            "name": file.split(sep)[-1],
            "result": FileUtils.verify_json(file)
        }

    for result in json_results_new:
        if not json_results_new.get(result).get("result"):
            if fallback_new_meta:
                print("Unable to load new fallback meta", json_results_new.get(result).get("name"))
            else:
                print("Unable to load new meta", json_results_new.get(result).get("name"))
            exit()

    print("Calculating changes")
    file_name_changes = {}

    latest_meta_names = {}
    new_meta_names = {}

    for l_result in json_results_latest:
        latest_meta_names[json_results_latest.get(l_result).get("name")] = l_result

    for n_result in json_results_new:
        new_meta_names[json_results_new.get(n_result).get("name")] = n_result

    if latest_meta_names.keys() != new_meta_names.keys():
        for name in latest_meta_names:
            if name not in new_meta_names:
                file_name_changes[name] = {"status":"REMOVED", "paths":{"before":latest_meta_names.get(name), "after":None}}

        for name in new_meta_names:
            if name not in latest_meta_names:
                file_name_changes[name] = {"status":"ADDED", "paths":{"before":None, "after":new_meta_names.get(name)}}

    for name in latest_meta_names:
        if name not in file_name_changes:
            file_name_changes[name] = {"status":"NONE", "paths":{"before":latest_meta_names.get(name), "after":new_meta_names.get(name)}}

    for name in new_meta_names:
        if name not in file_name_changes:
            file_name_changes[name] = {"status":"NONE", "paths":{"before":latest_meta_names.get(name), "after":new_meta_names.get(name)}}

    markdown = calculate_changes(tag, previous_tag, file_name_changes, file_meta, file_order, module_order, artifacts_detail_file)

    print("Moving changes to", markdown_output_file)
    FileUtils.write_file(markdown_output_file, markdown)

    print("Done!")

pre_main()