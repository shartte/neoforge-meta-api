package net.neoforged.meta.jobs;

import java.net.URI;

/**
 * Discover new versions of a Maven component using maven-metadata.xml.
 */
public class MavenVersionDiscoveryJob implements Runnable {
    private final URI mavenRepositoryUrl;

    private final String groupId;

    private final String artifactId;

    public MavenVersionDiscoveryJob(URI mavenRepositoryUrl, String groupId, String artifactId) {
        this.mavenRepositoryUrl = mavenRepositoryUrl;
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    @Override
    public void run() {

    }
}
