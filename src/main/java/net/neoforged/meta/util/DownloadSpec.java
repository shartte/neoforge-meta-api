package net.neoforged.meta.util;

import java.net.URI;

public interface DownloadSpec {
    String checksum();
    int size();
    URI uri();
    String checksumAlgorithm();
}
