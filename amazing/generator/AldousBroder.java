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

import static amazing.Utils.sample;

import amazing.grid.Cell;
import amazing.grid.Grid;

/**
 * Aldous-Broder algorithm for maze generation.
 */
public class AldousBroder<C extends Cell<C>> extends Generator<C> {

    public void accept(Grid<C> grid) {
        C cell = grid.getRandom();
        int unvisited = grid.getSize() - 1;

        while (unvisited > 0) {
            C neighbour = sample(cell.getNeighbours());
            if (neighbour.getLinks().isEmpty()) {
                cell.link(neighbour, true);
                unvisited--;
            }
            cell = neighbour;
        }
    }

    public String getName() { return "Aldous Broder"; }
}