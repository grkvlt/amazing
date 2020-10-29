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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import amazing.grid.Cell;
import amazing.grid.Grid;

/**
 * Kruskals algorithm for maze generation.
 */
public class Kruskals<C extends Cell<C>> extends Generator<C> {
    public class State {
        private List<List<C>> neighbors;
        private Map<C,Integer> setForCell;
        private Map<Integer,Set<C>> cellsInSet;

        public State(Grid<C> grid) {
            this.neighbors = new ArrayList<>();
            this.setForCell = new HashMap<>();
            this.cellsInSet = new HashMap<>();

            for (C cell : grid) {
                int set = setForCell.size();
                setForCell.put(cell, set);
                cellsInSet.put(set, new HashSet<>(Set.of(cell)));

                if (cell.hasSouth()) {
                    neighbors.add(List.of(cell, cell.getSouth().get()));
                }
                if (cell.hasEast()) {
                    neighbors.add(List.of(cell, cell.getEast().get()));
                }
            }
        }

        public boolean canMerge(C left, C right) {
            return setForCell.get(left).intValue() != setForCell.get(right).intValue();
        }

        public void merge(C left, C right) {
            left.link(right, true);

            int winner = setForCell.get(left);
            int loser = setForCell.get(right);

            Set<C> losers = cellsInSet.get(loser);
            if (Objects.isNull(losers)) losers = Set.of(right);

            Set<C> winners = cellsInSet.get(winner);
            if (Objects.isNull(winners)) winners = new HashSet<>(Set.of(left));

            for (C cell : losers) {
                winners.add(cell);
                setForCell.put(cell, winner);
            }

            cellsInSet.remove(loser);
        }

        public List<List<C>> getNeighbours() { return neighbors; }
    }

    private State state = null;
    private boolean keepState = false;

    public void setState(State state) { this.state = state; }
    public void resetState() { this.state = null; }
    public void keepState(boolean enable) { this.keepState = enable; }

    public void accept(Grid<C> grid) {
        if (Objects.isNull(state)) setState(new State(grid));

        List<List<C>> neighbours = state.getNeighbours();
        Collections.shuffle(neighbours);
        while (!neighbours.isEmpty()) {
            List<C> top = neighbours.remove(0);
            C left = top.get(0);
            C right = top.get(1);
            if (state.canMerge(left, right)) {
                state.merge(left, right);
            }
        }

        if (!keepState) resetState();
    }

    public String getName() { return "Kruskals"; }
}