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

/**
 * A single {@link Cell cell} in a Maze {@link Grid grid}.
 */
public interface Cell<C extends Cell<C>> {

    void link(C target, boolean bidi);
    void unlink(C target, boolean bidi);

    Set<C> getLinks();
    boolean hasLinks();

    boolean linked(C target);
    boolean linked(Optional<C> target);

    Set<C> getNeighbours();
    boolean hasNeighbours();

    boolean hasNorth();
    boolean hasSouth();
    boolean hasWest();
    boolean hasEast();

    Optional<C> getNorth();
    Optional<C> getSouth();
    Optional<C> getWest();
    Optional<C> getEast();

    void setNorth(Optional<C> north);
    void setSouth(Optional<C> south);
    void setWest(Optional<C> west);
    void setEast(Optional<C> east);

    int getRow();
    int getColumn();
}