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
package amazing.exec;

import java.awt.Frame;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import amazing.task.Display;
import amazing.Constants;

/**
 * An application that {@link Display displays} mazes on screen.
*/
public abstract class Application implements Runnable, Closeable {
    static {
        if (!System.getProperties().containsKey(Constants.WATERMARK_KEY))
            System.setProperty(Constants.WATERMARK_KEY, "false");
    }

    protected ExecutorService exec = Executors.newCachedThreadPool();
    protected List<Future<?>> tasks = new ArrayList<>();
    protected Frame root = Display.frame();

    /**
     * Adds a {@link KeyListener listener} to update {@link State shared state} on keypresses.
     */
    public abstract void addListener(Frame root, State state);

    /**
     * Executes the main {@link Thread thread} of the application.
     */
    @Override
    public abstract void run();

    /**
     * Called to free up resources and shut down running {@link Thread threads}.
     */
    @Override
    public void close() throws IOException {
        exec.shutdownNow();
        if (Objects.nonNull(root)) root.dispose();
        System.exit(0);
    }
}