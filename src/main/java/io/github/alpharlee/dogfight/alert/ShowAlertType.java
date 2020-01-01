package io.github.alpharlee.dogfight.alert;

import java.util.ArrayList;
import java.util.List;

public enum ShowAlertType {
    OFF("off", "none"),
    ONLY("only", "just"),
    GREATER("greater", "bigger", ">", ">="),
    LESSER("lesser", "smaller", "<", "<="),
    ALL("all", "everything");

    private String displayName;
    private List<String> synonyms;

    private ShowAlertType(String displayName, String... altNames) {
        synonyms = new ArrayList<String>();
        synonyms.add(displayName);

        for (String altName : altNames) {
            synonyms.add(altName);
        }

        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public List<String> getNames() {
        return this.synonyms;
    }

    /**
     * Find the alert type by one of its names
     *
     * @param searchName
     * @return ShowAlertType with specified name or null if none found
     * @author R Lee
     */
    public static ShowAlertType find(String searchName) {
        String name = searchName.toLowerCase();

        for (ShowAlertType type : ShowAlertType.values()) {
            if (type.getNames().contains(name)) {
                return type;
            }
        }

        return null;
    }
}
