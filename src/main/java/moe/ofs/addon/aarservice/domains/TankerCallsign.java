package moe.ofs.addon.aarservice.domains;

public enum TankerCallsign {
    TEXACO("Texaco", 1),
    ARCO("Arco", 2),
    SHELL("Shell", 3);

    private String displayName;
    private int index;

    TankerCallsign(String displayName, int index) {
        this.displayName = displayName;
        this.index = index;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
