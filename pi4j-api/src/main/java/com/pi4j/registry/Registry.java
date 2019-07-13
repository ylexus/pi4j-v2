package com.pi4j.registry;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (API)
 * FILENAME      :  Registry.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2019 Pi4J
 * %%
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

import com.pi4j.config.Config;
import com.pi4j.exception.NotInitializedException;
import com.pi4j.io.IO;
import com.pi4j.provider.Provider;
import com.pi4j.provider.exception.ProviderException;
import com.pi4j.registry.exception.RegistryException;

public interface Registry {

    <T extends IO> T create(String providerId, Config config, Class<T> type) throws RegistryException, ProviderException, NotInitializedException;
    <T extends IO> T create(Provider provider, Config config, Class<T> type) throws RegistryException, ProviderException;
    <T extends IO> T create(Config config, Class<T> type) throws RegistryException, ProviderException;

    boolean exists(String id, Class<? extends IO> type);
    boolean exists(String id);

    <T extends IO> T get(String id, Class<T> type) throws RegistryException;
    <T extends IO> T destroy(String id) throws RegistryException;
}
