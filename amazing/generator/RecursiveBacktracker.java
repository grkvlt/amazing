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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import amazing.grid.Cell;
import amazing.grid.Grid;

/**
 * Recursive backtracker algorithm for maze generation.
 */
public class RecursiveBacktracker<C extends Cell<C>> extends Generator<C> {
   
    private Optional<C> start;

    public RecursiveBacktracker() {
        this(null);
    }

    public RecursiveBacktracker(C start) {
        this.start = Optional.ofNullable(start);
    }

    public void accept(Grid<C> grid) {
        Deque<C> stack = new ArrayDeque<>();
        if (start.isEmpty()) {
            stack.push(grid.getRandom());
        } else {
            stack.push(start.get());
        }
        
        while (!stack.isEmpty()) {
            C current = stack.peek();
            List<C> neighbours = current.getNeighbours().stream()
                    .filter(n -> n.getLinks().isEmpty())
                    .collect(Collectors.toList());
            if (neighbours.isEmpty()) {
                stack.pop();
            } else {
                C neighbour = sample(neighbours);
                current.link(neighbour, true);
                stack.push(neighbour);
            }
        }
    }

    public String getName() { return "Recursive Backtracker"; }
}