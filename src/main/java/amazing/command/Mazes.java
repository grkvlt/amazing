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
import static amazing.Constants.fileFormat;
import static amazing.Constants.scale;
import static amazing.Utils.GENERATORS;
import static amazing.Utils.saveDir;
import static amazing.Utils.sample;
import static amazing.Utils.timestamp;
import static amazing.Utils.save;
import static amazing.Utils.title;
import static amazing.Utils.logger;
import static amazing.Utils.random;
import static amazing.Utils.choose;
import static amazing.Utils.ratio;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import amazing.generator.Generator;
import amazing.grid.Cell;
import amazing.grid.OverCell;
import amazing.grid.UnderCell;
import amazing.grid.WeaveGrid;
import amazing.task.Builder;
import amazing.task.Renderer;
import amazing.Constants;
import amazing.Constants.Colors;

@SuppressWarnings("unchecked")
public class Mazes<O extends OverCell<O, U>, U extends UnderCell<U, O>, C extends Cell<C>, W extends WeaveGrid<O, U>> implements Closeable, Callable<Void> {
    private ExecutorService exec = Executors.newSingleThreadExecutor();
    private Optional<PrintWriter> log = Optional.empty();
    private String fileName;
    private int n;
    private Generator<C> generator;

    public Mazes(int n, String fileName, Generator<C> generator) {
        this.n = n;
        this.fileName = fileName;
        this.generator = generator;
    }

    @Override
    public Void call() throws Exception {
        if (n > 1) log = Optional.of(logger(String.format("index-%s.txt", timestamp())));

        for (int i = 0; i < n; i++) {
            int rows = 50, columns = 80, size = 20;
            float inset = 0.1f;
            int color = Colors.choose();
            boolean dark = choose(10);

            if (i > 0) {
                rows = scale(random(20, 100));
                columns = scale(random(40, 200));
                size = scale(random(10, 25));
                inset = choose(20) ? 0f : 0.1f + ratio() / 5f;
                generator = (Generator<C>) sample(GENERATORS);
            }

            Builder<O, U, C, W> task = new Builder<>(rows, columns, generator);
            Renderer<O, U, C, W> renderer = new Renderer<>(size, inset, color, dark);

            Future<W> result = exec.submit(task);
            W grid = result.get();
            BufferedImage image = renderer.apply(grid);
            String file = save(image, fileFormat(), saveDir(), fileName);

            String title = title(grid);
            String data = String.format("%s :: %s", file, title);
            log.ifPresent(l -> l.println(data));
            if (DEBUG || n == 1) {
                System.out.printf("> %03d %s\n", i, data);
            } else {
                System.out.print(i == (n - 1) ? "\n" : i % 10 == 0 ? Integer.toString((i / 10) % 10) : ".");
            }
        }
        
        return (Void) null;
    }

    @Override
    public void close() throws IOException {
        exec.shutdownNow();
        log.ifPresent(l -> l.close());
        System.exit(0);
    }

    public static void main(String[] argv) throws Exception {
        if (DEBUG) {
            System.out.printf("+ Maze image file generator - %s\n", Constants.VERSION);
            System.out.printf("+ %s\n", Constants.COPYRIGHT);
        }

        String fileName = "maze";
        int n = 1;
        Generator<?> generator = (Generator<?>) sample(GENERATORS);

        // Parse arguments
        if (argv.length >= 1) {
            n = Integer.valueOf(argv[0]);
        }
        if (argv.length >= 2) {
            fileName = argv[1];
        }
        if (argv.length >= 3) {
            String className = "amazing.generator." + argv[2];
            try {
                Class<? extends Generator<?>> c = (Class<? extends Generator<?>>) Class.forName(className);
                Constructor<? extends Generator<?>> ctor = c.getConstructor();
                generator = ctor.newInstance();
                if (DEBUG) System.out.printf("- Generator %s loaded\n", generator.getName());
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException e) {
                String message = "Cannot load generator " + argv[2];
                if (DEBUG) System.err.printf("! %s: %s\n", message, e.getMessage());
                throw new IllegalArgumentException(message, e);
            }
        }

        try (Mazes<?,?,?,?> mazes = new Mazes<>(n, fileName, generator)) {
            mazes.call();
        }
    }
}