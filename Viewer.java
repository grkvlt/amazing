
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
import static amazing.Constants.DEBUG;

import java.awt.GraphicsDevice;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.util.List;
import java.util.concurrent.Future;

import amazing.Application;
import amazing.Constants;
import amazing.grid.Cell;
import amazing.grid.OverCell;
import amazing.grid.UnderCell;
import amazing.grid.WeaveGrid;
import amazing.task.Display;

public class Viewer<O extends OverCell<O, U>, U extends UnderCell<U, O>, C extends Cell<C>, W extends WeaveGrid<O, U>> extends Application {
    private int monitor;

    public Viewer(int monitor) {
        this.monitor = monitor;
    }

    @Override
    public void addListener(Frame root, State state) {
        root.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent event) {
                switch (event.getKeyChar()) {
                    case 'q':
                    case 'Q':
                        state.setQuitting();
                        break;
                    case 's':
                    case 'S':
                        state.setSaving();
                        break;
                    case 'n':
                    case 'N':
                        state.setSkip();
                        break;
                    case 'w':
                    case 'W':
                        state.setWaiting();
                        break;
                    case ' ':
                        state.setPaused();
                        break;
                }
            }
        });
    }

    @Override
    public void run() {
        State state = new State();

        List<GraphicsDevice> gds = Display.monitors();
        if (monitor < 0 || monitor >= gds.size()) {
            String error = String.format("Invalid monitor id %d", monitor);
            System.err.printf("? %s - %d monitors available\n", error, gds.size());
            throw new RuntimeException(error);
        }
        GraphicsDevice gd = gds.get(monitor);

        Display<O,U,C,W> display = new Display<>(gd, root, exec, state);
        addListener(root, state);

        Future<?> task = exec.submit(display);
        while (!task.isDone());
    }

    public static void main( String[] argv) throws Exception {
        if (DEBUG) {
            System.out.printf("+ Maze viewer application - %s\n", Constants.VERSION);
            System.out.printf("+ %s\n", Constants.COPYRIGHT);
        }

        int monitor = 0;

        // Parse arguments
        if (argv.length >= 1) {
            monitor = Integer.parseInt(argv[0]);
        }

        Viewer<?,?,?,?> viewer = new Viewer<>(monitor);
        viewer.run();
        viewer.close();
    }
}