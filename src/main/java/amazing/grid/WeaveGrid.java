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

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public class WeaveGrid<O extends OverCell<O,U>, U extends UnderCell<U,O>> extends Grid<O> {
    protected List<U> under;

    public WeaveGrid(int rows, int columns) {
        super(rows, columns);
    }

    @Override
    protected List<List<O>> prepare() {
        grid = new ArrayList<>();
        under = new ArrayList<>();
        for (int x = 0; x < rows; x++) {
            List<O> row = new ArrayList<>();
            for (int y = 0; y < columns; y++) {
                row.add((O) new OverCell<>(x, y, this));
            }
            grid.add(row);
        }
        return grid;
    }

    public void tunnelUnder(O over) {
        U cell = (U) new UnderCell<>(over);
        under.add(cell);
    }

    @Override
    public Iterator<O> iterator() {
        List<O> cells = new ArrayList<>();
        for (int x = 0; x < rows; x++) {
            cells.addAll(grid.get(x));
        }
        for (U cell : under) {
            cells.add((O) cell);
        }
        return cells.iterator();
    }
}