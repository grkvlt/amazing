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

import java.util.Optional;
import java.util.Set;

public class OverCell<O extends OverCell<O,U>, U extends UnderCell<U,O>> extends BasicCell<O> {
    protected WeaveGrid<O,U> grid;

    public OverCell(int row, int column, WeaveGrid<O,U> grid) {
        this(row, column);
        this.grid = grid;
    }
    private OverCell(int row, int column) {
        super(row, column);
    }

    public WeaveGrid<O,U> getGrid() { return grid; }

    @Override
    public Set<O> getNeighbours() {
        Set<O> neighbours = super.getNeighbours();

        if (canTunnelNorth()) neighbours.add(getNorth().get().getNorth().get());
        if (canTunnelSouth()) neighbours.add(getSouth().get().getSouth().get());
        if (canTunnelEast()) neighbours.add(getEast().get().getEast().get());
        if (canTunnelWest()) neighbours.add(getWest().get().getWest().get());
        
        return neighbours;
    }

    public boolean canTunnelNorth() {
        return north.isPresent() && north.get().north.isPresent() && north.get().hasHorizontalPassage();
    }
    public boolean canTunnelSouth() {
        return south.isPresent() && south.get().south.isPresent() && south.get().hasHorizontalPassage();
    }
    public boolean canTunnelEast() {
        return east.isPresent() && east.get().east.isPresent() && east.get().hasVerticalPassage();
    }
    public boolean canTunnelWest() {
        return west.isPresent() && west.get().west.isPresent() && west.get().hasVerticalPassage();
    }

    public boolean hasHorizontalPassage() {
        return linked(east) && linked(west) && !linked(north) && !linked(south);
    }
    public boolean hasVerticalPassage() {
        return linked(north) && linked(south) && !linked(east) && !linked(west);
    }

    @Override
    public void link(O target, boolean bidi) {
        Optional<O> neighbour = Optional.empty();
        if (north.isPresent() && north.get().hasNorth() && north.get().hasSouth() &&
                target.south.isPresent() && north.get().equals(target.south.get())) {
            neighbour = north;
        } else if (south.isPresent() && south.get().hasSouth() && south.get().hasNorth() &&
                target.north.isPresent() && south.get().equals(target.north.get())) {
            neighbour = south;
        } else if (east.isPresent() && east.get().hasEast() && east.get().hasWest() &&
                target.west.isPresent() && east.get().equals(target.west.get())) {
            neighbour = east;
        } else if (west.isPresent() && west.get().hasWest() && west.get().hasEast() &&
                target.east.isPresent() && west.get().equals(target.east.get())) {
            neighbour = west;
        }
        
        if (neighbour.isPresent()) {
            O cell = neighbour.get();
            grid.tunnelUnder(cell);
        } else {
            super.link(target, bidi);
        }
    }
}