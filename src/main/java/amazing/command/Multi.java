
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
package amazing.command;

import static amazing.Constants.DEBUG;

import java.awt.GraphicsDevice;
import java.awt.Frame;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import amazing.Constants;
import amazing.grid.Cell;
import amazing.grid.OverCell;
import amazing.grid.UnderCell;
import amazing.grid.WeaveGrid;
import amazing.task.Display;
import amazing.exec.Application;
import amazing.exec.State;

public class Multi<O extends OverCell<O, U>, U extends UnderCell<U, O>, C extends Cell<C>, W extends WeaveGrid<O, U>> extends Application {
    static {
        System.setProperty(Constants.FULLSCREEN_KEY, "true");
    }

    private List<Frame> frames = new ArrayList<>();

    @Override
    public void run() {
        List<GraphicsDevice> gds = Display.monitors();
        for (GraphicsDevice gd : gds) {
            State state = new State();
            Frame root = Display.frame(gd);
            Display<O,U,C,W> display = new Display<>(gd, root, exec, state);
            addListener(root, state);
            frames.add(root);

            Future<?> task = exec.submit(display);
            tasks.add(task);
        }

        while (tasks.stream().noneMatch(f -> f.isDone()));
    }

    @Override
    public void close() throws IOException {
        for (Frame root : frames) {
            root.dispose();
        }
        super.close();
    }

    public static void main( String[] argv) throws Exception {
        if (DEBUG) {
            System.out.printf("+ Multi screen maze viewer application - %s\n", Constants.VERSION);
            System.out.printf("+ %s\n", Constants.COPYRIGHT);
        }

        try (Multi<?,?,?,?> multi = new Multi<>()) {
            multi.run();
        }
    }
}