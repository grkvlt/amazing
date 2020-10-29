
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
import static amazing.Constants.saveDir;
import static amazing.Constants.scale;
import static amazing.Utils.GENERATORS;
import static amazing.Utils.sample;
import static amazing.Utils.timestamp;
import static amazing.Utils.save;
import static amazing.Utils.title;
import static amazing.Utils.logger;
import static amazing.Utils.random;
import static amazing.Utils.choose;
import static amazing.Utils.ratio;

import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import amazing.generator.Generator;
import amazing.grid.Cell;
import amazing.grid.OverCell;
import amazing.grid.UnderCell;
import amazing.grid.WeaveGrid;
import amazing.Builder;
import amazing.Constants;
import amazing.Renderer;

public class Mazes {
    @SuppressWarnings("unchecked")
    public static <O extends OverCell<O, U>, U extends UnderCell<U, O>, C extends Cell<C>, W extends WeaveGrid<O, U>> void main( String[] argv) throws Exception {
        if (DEBUG) {
            System.out.printf("+ Maze image generator - %s\n", Constants.VERSION);
            System.out.printf("+ %s\n", Constants.COPYRIGHT);
        }

        String fileName = "maze";
        int n = 1;
        Generator<C> generator = (Generator<C>) sample(GENERATORS);

        ExecutorService EXEC = Executors.newSingleThreadExecutor();
        Optional<PrintWriter> LOG = Optional.empty();

        // Parse arguments
        if (argv.length >= 1) {
            n = Integer.valueOf(argv[0]);
        }
        if (argv.length >= 2) {
            fileName = argv[1];
        }
        if (argv.length >= 3) {
            String className = "amazing.generator." + argv[2];
            Class<? extends Generator<C>> c = (Class<? extends Generator<C>>) Class.forName(className);
            Constructor<? extends Generator<C>> ctor = c.getConstructor();
            generator = ctor.newInstance();
            if (DEBUG) System.out.printf("- Generator %s loaded\n", generator.getName());
        }

        if (n > 1) LOG = Optional.of(logger(String.format("index-%s.txt", timestamp())));

        for (int i = 0; i < n; i++) {
            int rows = 50, columns = 80, size = 20;
            float inset = 0.1f;
            int color = random(6);
            boolean dark = choose(10);

            if (n > 1) {
                rows = scale(random(20, 100));
                columns = scale(random(40, 200));
                size = scale(random(10, 25));
                inset = choose(20) ? 0f : 0.1f + ratio() / 5f;
                if (argv.length < 3)
                    generator = (Generator<C>) sample(GENERATORS);
            }

            Builder<O, U, C, W> task = new Builder<>(rows, columns, generator);
            Renderer<O, U, C, W> renderer = new Renderer<>(size, inset, color, dark);

            Future<W> result = EXEC.submit(task);
            W grid = result.get();
            BufferedImage image = renderer.apply(grid);
            String file = save(image, Constants.PNG, saveDir(), fileName);

            String title = title(grid);
            String data = String.format("%s :: %s", file, title);
            LOG.ifPresent(l -> l.println(data));
            if (DEBUG || n == 1) {
                System.out.printf("> %03d %s\n", i, data);
            } else {
                System.out.print(i == (n - 1) ? "\n" : i % 10 == 0 ? Integer.toString((i / 10) % 10) : ".");
            }
        }

        EXEC.shutdown();
        LOG.ifPresent(l -> l.close());
    }
}