/*
#
# Copyright 2013 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: data-api
# File:  ParameterContainerSinglton.java
# Description: This class is a singleton implementation of the ParameterContainer interface
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a singleton implementation of the ParameterContainer interface
 * 
 * @author Yiming Sun
 *
 */
public final class ParameterContainerSingleton implements ParameterContainer {
    
    private Map<String, String> paramsMap = null;
    
    private static ParameterContainerSingleton instance = null;
    
    /**
     * Private constructor used by the singleton
     */
    private ParameterContainerSingleton() {
        this.paramsMap = new HashMap<String, String>();
    }
    
    /**
     * Method to get the singleton instance of this class. Instantiate the singleton instance if necessary.
     * 
     * @return the singleton instance of this class.
     */
    public static synchronized ParameterContainerSingleton getInstance() {
        if (instance == null) {
            instance = new ParameterContainerSingleton();
        }
        return instance;
    }
    
    /**
     * @see edu.indiana.d2i.htrc.access.ParameterContainer#getParameter(java.lang.String)
     */
    @Override
    public String getParameter(String parameterName) {
        String value = paramsMap.get(parameterName);
        return value;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.ParameterContainer#setParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void setParameter(String name, String value) {
        if (name != null && value != null) {
            paramsMap.put(name, value);
        }
    }

}

