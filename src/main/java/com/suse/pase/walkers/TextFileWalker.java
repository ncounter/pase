package com.suse.pase.walkers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Walks a directory (recursively), allowing a consumer to read any text file inside of it.
 * Also visits any archives found in the directory, up to a specified recursion level of nested archives, and allows the same consumer to read any text file in them.
 */
public class TextFileWalker {

    private static Logger LOG = Logger.getLogger(TextFileWalker.class.getName());
    private static byte[] buf = new byte[4096];

    private final Path root;
    private final int recursionLimit;

    public TextFileWalker(Path path, int recursionLimit) {
        this.root = path;
        this.recursionLimit = recursionLimit;
    }

    /**
     * Calls the consumer for all text files in the directory, and also any text file in any archive found in the directory,
     * up to the specified recursion level of nested archives.
     * The consumer receives each file's path and a stream of its bytes.
     */
    public void withTextFilesIn(WalkerConsumer consumer) {
        new DirectoryWalker(root).withFilesIn((path, stream) -> {
            if (isText(path, stream)) {
                consumer.accept(path, stream);
            }
            else if (recursionLimit >=1) {
                System.out.println("Processing: " + path);
                walkArchive(path, stream, recursionLimit, consumer);
            }
        });
    }

    private void walkArchive(Path archivePath, BufferedInputStream archiveStream, int recursionLevel, WalkerConsumer consumer) {
        new ArchiveWalker(archivePath, archiveStream).withFilesIn((path, stream) -> {
            if (isText(path, stream)) {
                consumer.accept(path, stream);
            }
            else if (recursionLevel > 1) {
                walkArchive(path, stream, recursionLevel - 1, consumer);
            }
        });
    }

    private boolean isText(Path path, BufferedInputStream stream) {
        // same heuristic used by diff
        // https://dev.to/sharkdp/what-is-a-binary-file-2cf5
        stream.mark(buf.length);
        try {
            var count = stream.read(buf, 0, buf.length);
            for (int i = 0; i < count; i++) {
                if (buf[i] == 0) {
                    stream.reset();
                    return false;
                }
            }
            return true;
        }
        catch (IOException e) {
            LOG.warning("I/O error during text file check on " + path + ", skipping");
            return false;
        }
        finally {
            try {
                stream.reset();
            }
            catch (IOException e) {
                // cannot happen in BufferedInputStream with mark >= 0
            }
        }
    }
}