package com.pi4j.annotation.processor.event;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (API)
 * FILENAME      :  DigitalChangeEventProcessor.java
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

import com.pi4j.Pi4J;
import com.pi4j.annotation.OnEvent;
import com.pi4j.annotation.exception.AnnotationException;
import com.pi4j.exception.NotInitializedException;
import com.pi4j.io.gpio.digital.Digital;
import com.pi4j.io.gpio.digital.DigitalChangeEvent;
import com.pi4j.io.gpio.digital.DigitalChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DigitalChangeEventProcessor implements OnEventProcessor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void process(Object instance, Method method, OnEvent annotation) throws Exception {
        try {
            // get digital I/O instance from registry
            Digital digital = Pi4J.registry().get(annotation.value(), Digital.class);

            // register a digital change event listener on this digital I/O instance
            digital.addListener((DigitalChangeListener) event -> {
                try {
                    method.trySetAccessible();
                    method.invoke(instance, event);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    logger.error(e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    logger.error(e.getMessage(), e);
                }
            });
        } catch (NotInitializedException e) {
            logger.error(e.getMessage(), e);
            throw new AnnotationException(e);
        }
    }

    @Override
    public Class getEventType() {
        return DigitalChangeEvent.class;
    }


}