/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.log4j.chainsaw.plugins;

import org.apache.log4j.chainsaw.prefs.SettingsManager;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * A factory class to create a Classloader that can refenerence jars/classes/resources
 * within a users plugin directory.
 * <p>
 * Currently a work in progress to see if this allows external jars required by
 * some 3rd party vendors for things like the JMSReceiver.
 *
 * @author psmith
 */
public class PluginClassLoaderFactory {
    private final ClassLoader pluginClassLoader;

    private static final PluginClassLoaderFactory instance = new PluginClassLoaderFactory();

    private PluginClassLoaderFactory() {
        this.pluginClassLoader = PluginClassLoaderFactory.create(new File(SettingsManager.getInstance().getSettingsDirectory() + File.separator + "plugins"));

    }

    public static PluginClassLoaderFactory getInstance() {
        return instance;
    }

    public ClassLoader getClassLoader() {
        return this.pluginClassLoader;
    }

    /**
     * Creates a Classloader that will be able to access any of the classes found
     * in any .JAR file contained within the specified directory path, PLUS
     * the actual Plugin directory itself, so it acts like the WEB-INF/classes directory,
     * any class file in the directory will be accessible
     *
     * @param pluginDirectory
     * @return
     * @throws IllegalArgumentException if the pluginDirectory is null, does not exist, or cannot be read
     * @throws RuntimeException         if turning a File into a URL failed, which would be very unexpected
     */
    private static final ClassLoader create(File pluginDirectory) {
        if (pluginDirectory == null || !pluginDirectory.exists() || !pluginDirectory.canRead()) {
            return PluginClassLoaderFactory.class.getClassLoader();
        }

        String[] strings = pluginDirectory.list((dir, name) -> name.toUpperCase().endsWith(".JAR"));


        List<URL> list = new ArrayList<>();
        // add the plugin directory as a resource loading path
        try {
            list.add(pluginDirectory.toURI().toURL());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        if (strings != null) {
            for (String name : strings) {
                File file = new File(pluginDirectory, name);
                try {
                    list.add(file.toURI().toURL());
                    System.out.println("Added " + file.getAbsolutePath()
                        + " to Plugin class loader list");
                } catch (Exception e) {
                    System.err.println("Failed to retrieve the URL for file: "
                        + file.getAbsolutePath());
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
        ClassLoader parent = PluginClassLoaderFactory.class.getClassLoader();
        URL[] urls = (URL[]) list.toArray(new URL[list.size()]);
        return new URLClassLoader(urls, parent);
    }

}
