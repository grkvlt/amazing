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

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unchecked")
public class BasicCell<B extends BasicCell<B>> implements Cell<B> {
    protected int row, column;
    protected Optional<B> north = Optional.empty(), south = Optional.empty(), east = Optional.empty(), west = Optional.empty();
    protected Set<B> links;

    public BasicCell(int row, int column) {
        this.row = row;
        this.column = column;
        this.links = new LinkedHashSet<>();
    }

    public void link(B target) { link(target, true); }

    public void link(B target, boolean bidi) {
        getLinks().add(target);
        if (bidi) target.link((B) this, false);
    }

    public void unlink(B target, boolean bidi) {
        links.remove(target);
        if (bidi) target.unlink((B) this, false);
    }

    public Set<B> getLinks() { return links; }
    public boolean hasLinks() { return getLinks().size() > 0; }

    public boolean linked(B target) { return links.contains(target); }
    public boolean linked(Optional<B> target) { return target.isEmpty() ? false : linked(target.get()); }

    public Set<B> getNeighbours() {
        Set<B> neighbours = new LinkedHashSet<>();
        north.ifPresent(c -> neighbours.add(c));
        south.ifPresent(c -> neighbours.add(c));
        west.ifPresent(c -> neighbours.add(c));
        east.ifPresent(c -> neighbours.add(c));
        return neighbours;
    }
    public boolean hasNeighbours() { return getNeighbours().size() > 0; }

    public boolean hasNorth() { return north.isPresent(); }
    public boolean hasSouth() { return south.isPresent(); }
    public boolean hasWest() { return west.isPresent(); }
    public boolean hasEast() { return east.isPresent(); }

    public Optional<B> getNorth() { return north; }
    public Optional<B> getSouth() { return south; }
    public Optional<B> getWest() { return west; }
    public Optional<B> getEast() { return east; }

    public void setNorth(Optional<B> north) { this.north = north; }
    public void setSouth(Optional<B> south) { this.south = south; }
    public void setWest(Optional<B> west) { this.west = west; }
    public void setEast(Optional<B> east) { this.east = east; }

    public int getRow() { return row; }
    public int getColumn() { return column; }

    public String toString() {
        return String.format("Cell at (%d, %d)", column, row);
    }

    public boolean equals(Object other) {
        return other instanceof BasicCell &&
                getClass().equals(other.getClass()) &&
                this.row == ((B) other).row &&
                this.column == ((B) other).column;
    }
}