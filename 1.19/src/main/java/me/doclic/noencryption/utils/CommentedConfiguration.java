package me.doclic.noencryption.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.HashMap;

/**
 * @author dumptruckman, LlmDL & Articdive
 */
public class CommentedConfiguration extends YamlConfiguration {
    private final DumperOptions yamlOptions = new DumperOptions();
    private final Representer yamlRepresenter = new YamlRepresenter();
    private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);
    private HashMap<String, String> comments;
    private File file;

    public CommentedConfiguration(File file) {
        super();
        //this.load(file);
        comments = new HashMap<>();
        this.file = file;
    }

    public boolean load() {
        boolean loaded = true;

        try {
            this.load(file);
        } catch (InvalidConfigurationException | IOException e) {
            loaded = false;
        }

        return loaded;
    }

    public void save() {
        boolean saved = true;

        // Save the config just like normal
        try {
            /*
             * Doing some saving of our own. We have found that the implementation of YAMLComfiguration used by Bukkit will attempt to
             * cap strings at 80 characters long, forming new lines in some of our longer strings (channel_formats.)
             */
            this.save(file);
        } catch (Exception e) {
            saved = false;
        }

        // if there's comments to add and it saved fine, we need to add comments
        if (!comments.isEmpty() && saved) {
            // String array of each line in the config file
            String[] yamlContents = FileMgmt.convertFileToString(file).split("[" + System.getProperty("line.separator") + "]");

            // This will hold the newly formatted line
            StringBuilder newContents = new StringBuilder();
            // This holds the current path the lines are at in the config
            String currentPath = "";
            // This flags if the line is a node or unknown text.
            boolean node;
            // The depth of the path. (number of words separated by periods - 1)
            int depth = 0;

            // Loop through the config lines
            for (String line : yamlContents) {
                // If the line is a node (and not something like a list value)
                if (line.contains(": ") || (line.length() > 1 && line.charAt(line.length() - 1) == ':')) {
                    // This is a node so flag it as one
                    node = true;

                    // Grab the index of the end of the node name
                    int index;
                    index = line.indexOf(": ");
                    if (index < 0) {
                        index = line.length() - 1;
                    }
                    // If currentPath is empty, store the node name as the currentPath. (this is only on the first iteration, i think)
                    if (currentPath.isEmpty()) {
                        currentPath = line.substring(0, index);
                    } else {
                        // Calculate the whitespace preceding the node name
                        int whiteSpace = 0;
                        for (int n = 0; n < line.length(); n++) {
                            if (line.charAt(n) == ' ') {
                                whiteSpace++;
                            } else {
                                break;
                            }
                        }
                        // Find out if the current depth (whitespace * 2) is greater/lesser/equal to the previous depth
                        if (whiteSpace / 2 > depth) {
                            // Path is deeper.  Add a . and the node name
                            currentPath += "." + line.substring(whiteSpace, index);
                            depth++;
                        } else if (whiteSpace / 2 < depth) {
                            // Path is shallower, calculate current depth from whitespace (whitespace / 2) and subtract that many levels from the currentPath
                            int newDepth = whiteSpace / 2;
                            for (int i = 0; i < depth - newDepth; i++) {
                                currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                            }
                            // Grab the index of the final period
                            int lastIndex = currentPath.lastIndexOf(".");
                            if (lastIndex < 0) {
                                // if there isn't a final period, set the current path to nothing because we're at root
                                currentPath = "";
                            } else {
                                // If there is a final period, replace everything after it with nothing
                                currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                                currentPath += ".";
                            }
                            // Add the new node name to the path
                            currentPath += line.substring(whiteSpace, index);
                            // Reset the depth
                            depth = newDepth;
                        } else {
                            // Path is same depth, replace the last path node name to the current node name
                            int lastIndex = currentPath.lastIndexOf(".");
                            if (lastIndex < 0) {
                                // if there isn't a final period, set the current path to nothing because we're at root
                                currentPath = "";
                            } else {
                                // If there is a final period, replace everything after it with nothing
                                currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                                currentPath += ".";
                            }
                            //currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                            currentPath += line.substring(whiteSpace, index);
                        }
                    }
                } else
                    node = false;
                if (node) {
                    String comment;
                    // If there's a comment for the current path, retrieve it and flag that path as already commented
                    comment = comments.get(currentPath);
                    if (comment != null) {
                        // Add the comment to the beginning of the current line
                        line = comment + System.getProperty("line.separator") + line + System.getProperty("line.separator");
                    } else {
                        // Add a new line as it is a node, but has no comment
                        line += System.getProperty("line.separator");
                    }
                }
                // Add the (modified) line to the total config String
                newContents.append(line).append((!node) ? System.getProperty("line.separator") : "");
            }
            /*
             * Due to a bukkit bug we need to strip any extra new lines from the
             * beginning of this file, else they will multiply.
             */
            while (newContents.toString().startsWith(System.getProperty("line.separator")))
                newContents = new StringBuilder(newContents.toString().replaceFirst(System.getProperty("line.separator"), ""));

            // Write the string to the config file
            FileMgmt.stringToFile(newContents.toString(), file);
        }
    }

    /**
     * Adds a comment just before the specified path. The comment can be
     * multiple lines. An empty string will indicate a blank line.
     *
     * @param path         Configuration path to add comment.
     * @param commentLines Comments to add. One String per line.
     */
    public void addComment(String path, String... commentLines) {
        StringBuilder commentstring = new StringBuilder();
        StringBuilder leadingSpaces = new StringBuilder();
        for (int n = 0; n < path.length(); n++) {
            if (path.charAt(n) == '.') {
                leadingSpaces.append("  ");
            }
        }
        for (String line : commentLines) {
            if (!line.isEmpty()) {
                line = leadingSpaces + line;
            } else {
                line = "";
            }
            if (commentstring.length() > 0) {
                commentstring.append(System.getProperty("line.separator"));
            }
            commentstring.append(line);
        }
        comments.put(path, commentstring.toString());
    }

    public void save(File file) throws IOException {
        Validate.notNull(file, "File cannot be null");

        Files.createParentDirs(file);

        String data = this.saveToString();

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
            writer.write(data);
        }
    }

    @Override
    public String saveToString() {
        yamlOptions.setIndent(options().indent());
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlOptions.setWidth(10000);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        String dump = yaml.dump(getValues(false));

        if (dump.equals(BLANK_CONFIG)) {
            dump = "";
        }

        return dump;
    }
}