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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Runner {
    public static void main(String[] argv) throws Exception {
        // Copy arguments
        List<String> args = new ArrayList<>();
        args.addAll(Arrays.asList(argv));

        // Parse arguments
        String command = "Mazes";
        if (argv.length > 0) {
            command = args.remove(0);
        }

        try {
            // Locate command
            String className = "amazing.command." + command;
            Class<?> c = (Class<?>) Class.forName(className);
            Method main = c.getMethod("main", argv.getClass());
            Supplier<Integer> mods =  () -> main.getModifiers();

            if (Objects.isNull(main) || !(Modifier.isPublic(mods.get()) && Modifier.isStatic(mods.get()))) {
                throw new IllegalArgumentException("Cannot find usable main method for " + command);
            }

            // Execute command
            main.invoke(null, new Object[] { args.toArray(new String[0])});
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException("Cannot load class for " + command, cnfe);
        }
    }
}