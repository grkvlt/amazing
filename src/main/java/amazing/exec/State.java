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
package amazing.exec;

import static java.lang.Thread.State.TIMED_WAITING;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The {@link Application} state.
 * 
 * State is stored as a {@link Map} of String keys and arbitrary values,
 * but with the emphasis on {@link Boolean boolean flags}.
 */
public class State {
    public static final String QUIT = "QUIT";
    public static final String SAVE = "SAVE";
    public static final String PAUSE = "PAUSE";
    public static final String NEXT = "NEXT";
    public static final String WAIT = "WAIT";
    public static final String THREAD = "THREAD";
    public static final Set<String> FLAGS = Set.of(QUIT, SAVE, PAUSE, NEXT, WAIT);

    private Map<String,Object> state;
    private Optional<State> root = Optional.empty();
    private Optional<Consumer<State>> listener = Optional.empty();

    
    public State() {
        this.state = new HashMap<>();
    }

    public State(State root) {
        this();
        this.root = Optional.of(root);
    }

    public void setStateChangedListener(Consumer<State> listener) {
        this.listener = Optional.of(listener);
    }

    public boolean has(String key) { return state.containsKey(key); }
    public boolean get(String key) { return get(key, Boolean.FALSE); }
    @SuppressWarnings("unchecked")
    public <O> O get(String key, O def) {
        return (O) root
            .filter(r -> r.has(key))    // if key is set on root
            .map(r -> r.state)          // then use root map
            .orElse(state)              // else use this map
            .getOrDefault(key, def);    // get key from map or return false
    }
    public void set(String key, Object value) {
        state.put(key, value);
        listener.ifPresent(v -> v.accept(this));
    }
    public void set(String key) { set(key, Boolean.TRUE); }
    public void unset(String key) { set(key, Boolean.FALSE); }
    public void reset() {
        state.clear();
        root.ifPresent(s -> s.reset());
    }

    public boolean quitting() { return get(QUIT); }
    public boolean saving() { return get(SAVE); }
    public boolean paused() { return get(PAUSE); }
    public boolean skip() { return get(NEXT); }
    public boolean waiting() { return get(WAIT); }
    public Thread thread() { return get(THREAD, (Thread) null); }
    
    public void setQuitting() {
        set(QUIT);
        set(NEXT);
        unset(WAIT);
        unset(PAUSE);

        Thread thread = thread();
        if (thread != null) thread.interrupt();
    }
    
    public void setSaving() {
        set(SAVE);
    }
    
    public void setPaused() {
        set(PAUSE, !get(PAUSE));
    }
    
    public void setSkip() {
        set(NEXT);
        unset(WAIT);
        unset(PAUSE);

        Thread thread = thread();
        if (thread.getState() == TIMED_WAITING) {
            thread.interrupt();
        }
    }

    public void setWaiting() {
        set(WAIT);
        unset(NEXT);
    }
    
    public String toString() {
        final StringBuilder out = new StringBuilder();
        out.append("{ ");
        Iterator<String> keys = state.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            out.append(key)
                .append("=")
                .append(Objects.toString(state.get(key)));
            if (keys.hasNext()) out.append(", ");
        }
        root.ifPresent(s -> {
            out.append(", ")
                .append(s.toString());
        });
        out.append(" }");
        return out.toString();
    }
}