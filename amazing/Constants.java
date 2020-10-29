/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 * Copyright 2020 by Andrew Donald Kennedy
 */
package amazing;

import static amazing.Utils.random;

import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration and system property constant definitiomns.
 */
public class Constants {
    // Properties for global runtime configuration
    public static final String DEBUG_KEY = "amazing.debug";
    public static final String SEED_KEY = "amazing.seed";
    public static final String WATERMARK_KEY = "amazing.watermark";
    public static final String SCALE_KEY = "amazing.scale";
    public static final String SAVE_DIR_KEY = "amazing.save";

    // Properties for Viewer application configuration
    public static final String PAUSE_MIN_KEY = "amazing.viewer.pause.min";
    public static final String PAUSE_MAX_KEY = "amazing.viewer.pause.max";
    public static final String FONT_KEY = "amazing.viewer.font";
    public static final String ZOOM_KEY = "amazing.viewer.zoom";
    public static final String FULLSCREEN_KEY = "amazing.viewer.fullscreen";

    /** Default save directory in {@code user.home} */
    public static final String SAVE_DIR = "Amazing";

    /** Debugging enable */
    public static final Boolean DEBUG = propertyFlag(DEBUG_KEY, false);

    /** Image save formats */
    public static final String PNG = "PNG", JPEG = "JPEG", TIFF = "TIFF";
    
    /** Copyright text */
    public static final String COPYRIGHT = "Copyright 2020 by Andrew Donald Kennedy";

    /** Version text */
    public static final String VERSION = "Amazing 0.7.6";

    /** Format for {@link Utils#timestamp()} output */
    public static final String TIMESTAMP = "yyyyMMdd-HHmmss";

    /** Font to use in {@link Viewer} for titles */
    public static final String TITLE_FONT = "Calibri-bold-14";

    /** Font to use in {@link Viewer} for messages */
    public static final String MSG_FONT = "monospaced-bold-10";

    /** Shortest pause time (in seconds) for {@link Viewer} */
    public static final Integer MIN_PAUSE = 10;

    /** Longest pause time (in seconds) for {@link Viewer} */
    public static final Integer MAX_PAUSE = 20;

    public static Font font() {
        String name = System.getProperty(FONT_KEY, TITLE_FONT);
        return Font.decode(name);
    }

    public static int minPause() {
        return Integer.getInteger(PAUSE_MIN_KEY, MIN_PAUSE);
    }

    public static int maxPause() {
        return Integer.getInteger(PAUSE_MAX_KEY, MAX_PAUSE);
    }

    public static int pause() {
        return random(minPause(), maxPause());
    }

    /**
     * Look up a boolean {@code key} in the {@link System#getProperties() system properties}.
     * 
     * Returns {@literal true} if the flag exists and has no value, otherwise uses the standard
     * {@link System#getProperty(String, String)} method to get the value (using {@code def} if the
     * flag is not present) and returns it as a boolean.
     * 
     * @param flag The name of the flag propoerty to look up
     * @param def The default value if the flag is not present
     */
    public static boolean propertyFlag(String flag, boolean def) {
        if (System.getProperties().containsKey(flag) && System.getProperty(flag).isEmpty()) {
            return true;
        } else return Boolean.parseBoolean(System.getProperty(flag, Boolean.toString(def)));
    }

    public static boolean fullscreen() {
        return propertyFlag(FULLSCREEN_KEY, false);
    }

    public static boolean zoom() {
        return propertyFlag(ZOOM_KEY, true);
    }

    public static boolean watermark() {
        return propertyFlag(WATERMARK_KEY, true);
    }

    public static float scale() {
        return Float.parseFloat(System.getProperty(SCALE_KEY, "1.0"));
    }

    public static int scale(int value) {
        return (int) (value * scale());
    }

    /**
     * The directory to save files and images to.
     * 
     * The default is to use the directory named {@link #SAVE_DIR} in the
     * {@code user.home} directory. This will be created if it does
     * not exist. If the {@link #SAVE_DIR_KEY} property is set, this will
     * be used in preference, and will also be treated as a sub-directory
     * of the home directory unless an absolute path is given.
     * 
     * @return The name of the directory to use
     */
    public static String saveDir() {
        String home = System.getProperty("user.home");
        String save = System.getProperty(SAVE_DIR_KEY, SAVE_DIR);
        
        Path dir = Path.of(save).isAbsolute() ? Path.of(save) : Path.of(home, save);

        if (Files.notExists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException ioe) {
                String message = String.format("Failed to create %s directory: %s", dir, ioe.getMessage());
                System.err.println(message);
                throw new RuntimeException(message, ioe);
            }
        }

        return dir.toString();
    }
}