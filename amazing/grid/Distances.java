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
package amazing.grid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Distances<C extends Cell<C>> {
    private C root;
    private Optional<C> goal = Optional.empty();
    private Map<C,Integer> cells;

    public static <C extends Cell<C>> Distances<C> from(C root) {
        Distances<C> distances = new Distances<>(root);
        distances.calculate();
        return distances;
    }

    private Distances(C root) {
        this.root = root;
        this.cells = new HashMap<>();
        cells.put(root, 0);
    }

    private void calculate() {
        Set<C> frontier = new HashSet<>();
        frontier.add(root);

        while (!frontier.isEmpty()) {
            Set<C> newFrontier = new HashSet<>();
            for (C cell : frontier) {
                for (C linked : cell.getLinks()) {
                    if (!isSet(linked)) {
                        setDistance(linked, getDistance(cell) + 1);
                        newFrontier.add(linked);
                    }
                }
            }
            frontier = newFrontier;
        }
    }

    public boolean isSet(C cell) {
        return cells.containsKey(cell);
    }

    public Set<C> getCells() {
        return cells.keySet();
    }

    public int getDistance(C cell) {
        return cells.get(cell);
    }

    public void setDistance(C cell, int distance) {
        cells.put(cell, distance);
    }

    public C getRoot() { return root; }

    public Optional<C> getGoal() { return goal; }

    public Distances<C> to(C cell) {
        goal = Optional.of(cell);
        C current = cell;
        Distances<C> breadcrumbs = new Distances<>(root);
        breadcrumbs.setDistance(current, getDistance(current));

        do {
            for (C neighbour : current.getLinks()) {
                if (getDistance(neighbour) < getDistance(current)) {
                    breadcrumbs.setDistance(neighbour, getDistance(neighbour));
                    current = neighbour;
                    break;
                }
            }
        } while (!current.equals(root));

        return breadcrumbs;
    }

    public C getMax() {
        C max = root;

        for (C cell : cells.keySet()) {
            if (getDistance(cell) > getDistance(max)) {
                max = cell;
            }
        }

        return max;
    }
}