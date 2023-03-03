package me.doclic.noencryption.utils.updates;

import me.doclic.noencryption.NoEncryption;

public class PluginVersion {
    private final int numOne;
    private final int numTwo;

    public PluginVersion(
            int numOne,
            int numTwo
    ) {
        this.numOne = numOne;
        this.numTwo = numTwo;
    }

    public PluginVersion() {
        numOne = 0;
        numTwo = 0;
    }

    public int getNumOne() {
        return numOne;
    }

    public int getNumTwo() {
        return numTwo;
    }

    public int compare(PluginVersion compareTo) {
        if (getNumOne() == compareTo.getNumOne()) {
            return Integer.compare(getNumTwo(), compareTo.getNumTwo());
        } else {
            return Integer.compare(getNumOne(), compareTo.getNumTwo());
        }
    }

    public PluginVersion current() {
        return fromString(NoEncryption.plugin().getDescription().getVersion());
    }

    public PluginVersion fromString(String string) {
        return new PluginVersion(Integer.parseInt(string.split("\\.")[0]), Integer.parseInt(string.split("\\.")[1]));
    }

    @Override
    public String toString() {
        return numOne + "." + numTwo;
    }
}
