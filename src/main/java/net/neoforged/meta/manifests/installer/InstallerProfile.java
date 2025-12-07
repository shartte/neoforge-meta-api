package net.neoforged.meta.manifests.installer;

import tools.jackson.databind.ObjectMapper;

public class InstallerProfile {
    private static final ObjectMapper mapper = new ObjectMapper();

    private String json;
    private String version;
    private String minecraft;

    public static InstallerProfile from(String input) {
        return mapper.readValue(input, InstallerProfile.class);
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMinecraft() {
        return minecraft;
    }

    public void setMinecraft(String minecraft) {
        this.minecraft = minecraft;
    }
}
