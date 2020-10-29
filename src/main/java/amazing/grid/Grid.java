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

import static amazing.Utils.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;

public class Grid<C extends Cell<C>> implements Iterable<C> {
    public static final String LOOPED = "LOOPED";
    public static final String BRAIDED = "BRAIDED";
    public static final String CULLED = "CULLED";
    public static final String DELETED = "DELETED";
    public static final String GENERATOR = "GENERATOR";

    /** Keys for {@link #getMetadata(String)} storage. */
    public static final Set<String> METADATA_KEYS = Set.of(LOOPED, BRAIDED, CULLED, DELETED, GENERATOR);

    protected static final Random RANDOM = new Random();

    protected int rows, columns;
    protected List<List<C>> grid;
    protected Optional<Distances<C>> distances = Optional.empty();
    protected C farthest;
    protected int maximum;
    protected float braiding = 0f, culling = 0f, deleted = 0f;
    protected Map<String,Object> metadata = new HashMap<>();

    public Grid(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.grid = prepare();
        configure();
    }

    @SuppressWarnings("unchecked")
    protected List<List<C>> prepare() {
        grid = new ArrayList<>();
        for (int x = 0; x < rows; x++) {
            List<C> row = new ArrayList<>();
            for (int y = 0; y < columns; y++) {
                row.add((C) new BasicCell<>(x, y));
            }
            grid.add(row);
        }
        return grid;
    }

    protected void configure() {
        for (C cell : this) {
            int row = cell.getRow();
            int column = cell.getColumn();
            cell.setNorth(getCell(row - 1, column));
            cell.setSouth(getCell(row + 1, column));
            cell.setWest(getCell(row, column - 1));
            cell.setEast(getCell(row, column + 1));
        }
    }

    @Override
    public Iterator<C> iterator() {
        List<C> cells = new ArrayList<>();
        for (int x = 0; x < rows; x++) {
            cells.addAll(grid.get(x));
        }
        return cells.iterator();
    }

    public void setDistances(Distances<C> distances) {
        this.distances = Optional.of(distances);
        this.farthest = distances.getMax();
        this.maximum = distances.getDistance(farthest);
    }
    public Optional<Distances<C>> getDistances() { return distances; }

    public List<List<C>> getGrid() { return grid; }

    public Optional<C> getCell(int row, int column) {
        if (row < 0 || row >= rows)
            return Optional.empty();
        if (column < 0 || column >= columns)
            return Optional.empty();
        return Optional.of(grid.get(row).get(column));
    }

    public String getContents(C cell) {
        if (distances.isPresent() && distances.get().isSet(cell)) {
            int distance = distances.get().getDistance(cell);
            return String.format("%02x", distance);
        } else return "   ";
    }

    public C getRandom() {
        int row = RANDOM.nextInt(rows);
        int column = RANDOM.nextInt(columns);
        return getCell(row, column).get();
    }

    public List<C> getDeadends() {
        List<C> deadends = new ArrayList<>();
        for (C cell : this) {
            if (cell.getLinks().size() == 1)
                deadends.add(cell);
        }
        return deadends;
    }

    public float getBraiding() { return braiding; }
    public float getCulling() { return culling; }
    public float getDeleted() { return deleted; }
    public int getMaximum() { return maximum; }
    public C getFarthest() { return farthest; }

    public void setMetadata(String key) { metadata.put(key, null); }
    public void setMetadata(String key, Object data) { metadata.put(key, data); }
    public boolean hasMetadata(String key) { return metadata.containsKey(key); }
    public Optional<Object> getMetadata(String key) { return Optional.ofNullable(metadata.get(key)); }
    public String getMetadataString(String key) { return (String) getMetadata(key).orElse(""); }
    public Integer getMetadataInteger(String key) { return (Integer) getMetadata(key).orElse(0); }
    public Float getMetadataFloat(String key) { return (Float) getMetadata(key).orElse(0f); }

    public void braid(float p) {
        this.braiding = p;
        List<C> deadends = getDeadends();
        while (!deadends.isEmpty()) {
            C cell = deadends.remove(RANDOM.nextInt(deadends.size()));
            if (cell.getLinks().size() == 1 && RANDOM.nextFloat() <= p) {
                List<C> neighbours = cell.getNeighbours().stream()
                        .filter(n -> !cell.linked(n))
                        .collect(Collectors.toList());
                List<C> best = neighbours.stream()
                        .filter(n -> n.getLinks().size() == 1)
                        .collect(Collectors.toList());
                if (best.isEmpty()) best = neighbours;
                if (best.isEmpty()) continue;
                C neighbour = sample(best);
                cell.link(neighbour, true);
            }
        }
    }

    public void remove(C cell) {
        if (cell.hasLinks()) {
            for (C linked : cell.getLinks()) {
                cell.unlink(linked, true);
            }
        }

        cell.getNorth().ifPresent(n -> n.setSouth(Optional.empty()));
        cell.getSouth().ifPresent(n -> n.setNorth(Optional.empty()));
        cell.getWest().ifPresent(n -> n.setEast(Optional.empty()));
        cell.getEast().ifPresent(n -> n.setWest(Optional.empty()));
        cell.setNorth(Optional.empty());
        cell.setSouth(Optional.empty());
        cell.setWest(Optional.empty());
        cell.setEast(Optional.empty());
    }

    public void cull(float p) {
        this.culling = p;
        List<C> deadends = getDeadends();
        while (!deadends.isEmpty()) {
            C cell = deadends.remove(RANDOM.nextInt(deadends.size()));
            if (cell.getLinks().size() == 1 && RANDOM.nextFloat() <= p) {
                remove(cell);
            }
        }
    }

    public void delete(float p) {
        this.deleted = p;
        for (C cell : this) {
            if (RANDOM.nextFloat() <= p) {
                remove(cell);
            }
        }
    }

    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public int getSize() { return rows * columns; }

    @Override
    public String toString() {
        return String.format("A %smaze with %d rows and %d columns", hasMetadata(BRAIDED) ? "braided " : "", rows, columns);
    }
}