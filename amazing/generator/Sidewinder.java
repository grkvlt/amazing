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
package amazing.generator;

import static amazing.Utils.choose;
import static amazing.Utils.sample;

import java.util.ArrayList;
import java.util.List;

import amazing.grid.Cell;
import amazing.grid.Grid;

/**
 * Sidewinder algorithm for maze generation.
 */
public class Sidewinder<C extends Cell<C>> extends Generator<C> {
    
    public void accept(Grid<C> grid) {
        for (List<C> row : grid.getGrid()) {
            List<C> run = new ArrayList<>();
            for (C cell : row) {
                run.add(cell);
                boolean eastern = cell.getEast().isEmpty();
                boolean northern = cell.getNorth().isEmpty();
                boolean close = eastern || (!northern && choose());
                if (close) {
                    C member = sample(run);
                    member.getNorth().ifPresent(n -> member.link(n, true));
                    run.clear();
                } else {
                    cell.link(cell.getEast().get(), true);
                }
            }
        }
    }

    public String getName() { return "Sidewinder"; }
}