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
package amazing;

import static java.lang.Thread.State.TIMED_WAITING;
import static java.lang.Thread.State.RUNNABLE;

import java.awt.Frame;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import amazing.task.Display;

/**
 * An application that {@link Display displays} mazes on screen.
*/
public abstract class Application implements Runnable, Closeable {
    static {
        System.setProperty(Constants.WATERMARK_KEY, "false");
    }

    /**
     * The {@link Application} state.
     * 
     * State is stored as a {@link Map} of String keys and arbitrary values,
     * but with the emphasis on {@link Boolean boolean flags}.
     */
    public static class State {
        public static final String QUIT = "QUIT";
        public static final String SAVE = "SAVE";
        public static final String PAUSE = "PAUSE";
        public static final String SKIP = "SKIP";
        public static final String WAIT = "WAIT";
        public static final String THREAD = "THREAD";

        private Map<String,Object> state;
        private Optional<State> root = Optional.empty();
        
        public State() {
            this.state = new HashMap<>();
        }

        public State(State root) {
            this();
            this.root = Optional.of(root);
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
        public void set(String key, Object value) { state.put(key, value); }
        public void set(String key) { set(key, Boolean.TRUE); }
        public void unset(String key) { set(key, Boolean.FALSE); }
        public void reset() {
            state.clear();
            root.ifPresent(s -> s.reset());
        }

        public boolean quitting() { return get(QUIT); }
        public boolean saving() { return get(SAVE); }
        public boolean paused() { return get(PAUSE); }
        public boolean skip() { return get(SKIP); }
        public boolean waiting() { return get(WAIT); }
        public Thread thread() { return get(THREAD, (Thread) null); }
        
        public void setQuitting() {
            set(QUIT);
            set(SKIP);
            unset(WAIT);
            unset(PAUSE);

            Thread thread = thread();
            thread.interrupt();
        }
        
        public void setSaving() {
            set(SAVE);
        }
        
        public void setPaused() {
            set(PAUSE, !get(PAUSE));
        }
        
        public void setSkip() {
            set(SKIP);
            unset(WAIT);
            unset(PAUSE);

            Thread thread = thread();
            if (Set.of(TIMED_WAITING, RUNNABLE).contains(thread.getState())) {
                thread.interrupt();
            }
        }

        public void setWaiting() {
            set(WAIT);
            unset(SKIP);
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

    protected ExecutorService exec = Executors.newCachedThreadPool();
    protected List<Future<?>> tasks = new ArrayList<>();
    protected Frame root = Display.frame();

    /**
     * Adds a {@link KeyListener listener} to update {@link State shared state} on keypresses.
     */
    public abstract void addListener(Frame root, State state);

    /**
     * Executes the main {@link Thread thread} of the application.
     */
    @Override
    public abstract void run();

    /**
     * Called to free up resources and shut down running {@link Thread threads}.
     */
    @Override
    public void close() throws IOException {
        exec.shutdown();
        root.dispose();
        System.exit(0);
    }
}