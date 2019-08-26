package com.pi4j.io.pwm.impl;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (API)
 * FILENAME      :  DefaultPwmPreset.java
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

import com.pi4j.io.pwm.PwmPreset;

public class DefaultPwmPreset  implements PwmPreset {

    protected final String name;
    protected final Float dutyCycle;
    protected final Integer frequency;

    public DefaultPwmPreset(String name, Number dutyCycle){
        this.name = name.toLowerCase().trim();
        this.dutyCycle = dutyCycle.floatValue();
        this.frequency = null;
    }

    public DefaultPwmPreset(String name, Number dutyCycle, Integer frequency){
        this.name = name.toLowerCase().trim();

        // bounds check the duty-cycle value
        if(dutyCycle != null) {
            float dc = dutyCycle.floatValue();
            if (dc < 0) dc = 0;
            if (dc > 100) dc = 100;
            this.dutyCycle = dc;
        } else {
            this.dutyCycle = null;
        }
        this.frequency = frequency;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Float dutyCycle() {
        return this.dutyCycle;
    }

    @Override
    public Integer frequency() {
        return this.frequency;
    }
}
