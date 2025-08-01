package dev.ikm.maven.toolkit;

import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.util.io.FileUtil;
import dev.ikm.tinkar.entity.EntityService;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A proxy that centralizes and provides a simplified (closeable) entry to the Tinkar-Core Entity Provider
 */
public class DatastoreProxy implements Closeable {

    private static final String EPHEMERAL_DATASTORE_NAME = "Load Ephemeral Store";
    private static final String SPINED_ARRAY_DATASTORE_NAME = "Open SpinedArrayStore";

    public DatastoreProxy() {
        CachingService.clearAll();
        PrimitiveData.selectControllerByName(EPHEMERAL_DATASTORE_NAME);
        PrimitiveData.start();
    }

    public DatastoreProxy(File datastoreDirectory) {
        this(datastoreDirectory, false);
    }

    public DatastoreProxy(File datastoreDirectory, boolean clearDirectory) {
        if (!datastoreDirectory.exists()) {
            try {
                Files.createDirectories(datastoreDirectory.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (clearDirectory) {
                FileUtil.recursiveDelete(datastoreDirectory);
            }
        }
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, datastoreDirectory);
        PrimitiveData.selectControllerByName(SPINED_ARRAY_DATASTORE_NAME);
        PrimitiveData.start();
        EntityService.get().beginLoadPhase();
    }

    public void reload() {
        PrimitiveData.reload();
    }

    public void save() {
        PrimitiveData.save();
    }

    public boolean running () {
        return PrimitiveData.running();
    }

    @Override
    public void close() {
        EntityService.get().endLoadPhase();
        PrimitiveData.stop();
    }
}
