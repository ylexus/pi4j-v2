package com.pi4j.registry.impl;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (CORE)
 * FILENAME      :  DefaultRuntimeRegistry.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.pi4j.config.AddressConfig;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.LifecycleException;
import com.pi4j.io.IO;
import com.pi4j.io.exception.*;
import com.pi4j.runtime.Runtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>DefaultRuntimeRegistry class.</p>
 *
 * @author Robert Savage (<a href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public class DefaultRuntimeRegistry implements RuntimeRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRuntimeRegistry.class);
    private Runtime runtime;
    private final Map<String, IO> instances;
    private final Set<Integer> usedAddresses;

    // static singleton instance

    /**
     * <p>newInstance.</p>
     *
     * @param runtime a {@link com.pi4j.runtime.Runtime} object.
     *
     * @return a {@link com.pi4j.registry.impl.RuntimeRegistry} object.
     */
    public static RuntimeRegistry newInstance(Runtime runtime) {
        return new DefaultRuntimeRegistry(runtime);
    }

    // private constructor
    private DefaultRuntimeRegistry(Runtime runtime) {
        // set local runtime reference
        this.instances = new HashMap<>();
        this.usedAddresses = new HashSet<>();
        this.runtime = runtime;
    }

    @Override
    public synchronized RuntimeRegistry add(IO instance) throws IOInvalidIDException, IOAlreadyExistsException {

        // validate target I/O instance id
        String _id = validateId(instance.id());

        // first test to make sure this id does not already exist in the registry
        if (instances.containsKey(_id))
            throw new IOAlreadyExistsException(_id);
        if (instance.config() instanceof AddressConfig<?>) {
            AddressConfig<?> addressConfig = (AddressConfig<?>) instance.config();
            if (exists(addressConfig.address())) {
                throw new IOAlreadyExistsException(addressConfig.address());
            }
            this.usedAddresses.add(addressConfig.address());
        }

        // add instance to collection
        try {
            instance.initialize(this.runtime.context());
            instances.put(_id, instance);
        } catch (InitializeException e) {
            if (instance.config() instanceof AddressConfig<?>) {
                AddressConfig<?> addressConfig = (AddressConfig<?>) instance.config();
                this.usedAddresses.remove(addressConfig.address());
            }
            throw new IllegalStateException("Failed to initialize IO " + instance.getId(), e);
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized <T extends IO> T get(String id, Class<T> type)
        throws IOInvalidIDException, IONotFoundException {
        String _id = validateId(id);

        // first test to make sure this id is included in the registry
        if (!instances.containsKey(_id))
            throw new IONotFoundException(_id);
        return (T) instances.get(_id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized <T extends IO> T get(String id) throws IOInvalidIDException, IONotFoundException {
        String _id = validateId(id);

        // first test to make sure this id is included in the registry
        if (!instances.containsKey(_id))
            throw new IONotFoundException(_id);

        return (T) instances.get(_id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized <T extends IO> T remove(String id)
        throws IONotFoundException, IOInvalidIDException, IOShutdownException {
        String _id = validateId(id);
        IO shutdownInstance = null;

        // first test to make sure this id is included in the registry
        if (!exists(_id))
            throw new IONotFoundException(_id);

        // shutdown instance
        try {
            long start = System.currentTimeMillis();
            shutdownInstance = instances.get(_id);
            shutdownInstance.shutdown(runtime.context());
            long took = System.currentTimeMillis() - start;
            if (took > 10)
                logger.info("Shutting down of IO {} took {}ms", shutdownInstance.getId(), took);
        } catch (LifecycleException e) {
            logger.error(e.getMessage(), e);
            throw new IOShutdownException(shutdownInstance, e);
        }

        // remove the shutdown instance from the registry
        if (shutdownInstance.config() instanceof AddressConfig<?>) {
            AddressConfig<?> addressConfig = (AddressConfig<?>) shutdownInstance.config();
            this.usedAddresses.remove(addressConfig.address());
        }
        this.instances.remove(_id);

        // return the shutdown I/O provider instances
        return (T) shutdownInstance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean exists(String id) {
        String _id = null;
        try {
            _id = validateId(id);
            // return 'false' if the requested ID is not found
            // return 'true' if the requested ID is found
            return instances.containsKey(_id);
        } catch (IOInvalidIDException e) {
            return false;
        }
    }

    @Override
    public synchronized boolean exists(int address) {
        return usedAddresses.contains(address);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Map<String, ? extends IO> all() {
        return new HashMap<>(this.instances);
    }

    private String validateId(String id) throws IOInvalidIDException {
        if (id == null)
            throw new IOInvalidIDException();
        String validatedId = id.trim();
        if (validatedId.isEmpty())
            throw new IOInvalidIDException();
        return validatedId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized RuntimeRegistry shutdown() {
        all().values().forEach(instance -> {
            try {
                remove(instance.id());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        });
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RuntimeRegistry initialize() throws InitializeException {
        // NOTHING TO INITIALIZE
        return this;
    }
}
