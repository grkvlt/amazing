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
package amazing.task;

import static amazing.Utils.random;
import static amazing.Utils.choose;
import static amazing.Utils.ratio;

import java.util.concurrent.Callable;

import amazing.generator.BinaryTree;
import amazing.generator.Sidewinder;
import amazing.generator.RecursiveBacktracker;
import amazing.generator.Generator;
import amazing.generator.Kruskals;
import amazing.grid.Cell;
import amazing.grid.Distances;
import amazing.grid.OverCell;
import amazing.grid.UnderCell;
import amazing.grid.Grid;
import amazing.grid.WeaveGrid;

/**
 * Builds a maze of the specified grid size using the provided
 * {@link Generator generator algorithm}.
 */
@SuppressWarnings("unchecked")
public class Builder<O extends OverCell<O, U>, U extends UnderCell<U, O>, C extends Cell<C>, W extends WeaveGrid<O, U>> implements Callable<W> {
    private Generator<C> generator;
    private W grid;

    public Builder(int rows, int columns, Generator<C> generator) {
        this.grid = (W) new WeaveGrid<>(rows, columns);
        this.generator = generator;
    }

    @Override
    public W call() {
        if ((generator instanceof RecursiveBacktracker || generator instanceof Kruskals) && choose(10)) {
            float ratio = ratio() / 4f;
            grid.delete(ratio);
            grid.setMetadata(Grid.DELETED);
        }
        generator.accept((Grid<C>) grid);
        grid.setMetadata(Grid.GENERATOR, generator.getName());
        if ((generator instanceof BinaryTree || generator instanceof Sidewinder || generator instanceof Kruskals) && choose(2)) {
            int count = random(1, 4);
            for (int i = 0; i < count; i++) {
                generator.accept((Grid<C>) grid);
                grid.setMetadata(Grid.LOOPED, count);
            }
        }
        if (!grid.hasMetadata(Grid.DELETED) && choose(4)) {
            float ratio = ratio();
            int count = random(2, 6);
            for (int i = 0; i < count; i++) {
                grid.cull(ratio);
            }
            grid.setMetadata(Grid.CULLED, count);
        }
        if (choose()) {
            grid.braid(ratio());
            grid.setMetadata(Grid.BRAIDED);
        }

        if (!choose(10)) {
            O start = grid.getCell(grid.getRows() / 2, grid.getColumns() / 2).get();
            for (int i = 0; i < grid.getSize() / 5 && !start.hasLinks(); i++) start = grid.getRandom();
            if (start.hasLinks()) {
                Distances<O> distances = Distances.from(start);
                grid.setDistances(distances);
            }
        }

        return grid;
    }
}