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

public class UnderCell<U extends UnderCell<U,O>, O extends OverCell<O,U>> extends OverCell<O,U> {
    private O over;

    @SuppressWarnings("unchecked")
    public UnderCell(O over) {
        super(over.row, over.column, over.grid);

        this.over = over;

        if (over.hasHorizontalPassage()) {
            setNorth(over.getNorth());
            over.north.get().setSouth((Optional<O>) Optional.of(this));
            setSouth(over.getSouth());
            over.south.get().setNorth((Optional<O>) Optional.of(this));

            link(north.get(), true);
            link(south.get(), true);
        } else {
            setEast(over.getEast());
            over.east.get().setWest((Optional<O>) Optional.of(this));
            setWest(over.getWest());
            over.west.get().setEast((Optional<O>) Optional.of(this));

            link(east.get(), true);
            link(west.get(), true);
        }
    }

    public O getOver() { return over; }

    public boolean hasHorizontalPassage() {
        return east.isPresent() || west.isPresent();
    }

    public boolean hasVerticalPassage() {
        return north.isPresent() || south.isPresent();
    }
}