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
package amazing.task;

import static amazing.Constants.DEBUG;
import static amazing.Constants.ABOUT;
import static amazing.Constants.pause;
import static amazing.Constants.font;
import static amazing.Constants.fullscreen;
import static amazing.Constants.messages;
import static amazing.Constants.minPause;
import static amazing.Constants.scale;
import static amazing.Constants.zoom;
import static amazing.Utils.GENERATORS;
import static amazing.Utils.saveDir;
import static amazing.Utils.sample;
import static amazing.Utils.random;
import static amazing.Utils.choose;
import static amazing.Utils.ratio;
import static amazing.Utils.save;
import static amazing.Utils.sleep;
import static amazing.Utils.title;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import amazing.Constants;
import amazing.exec.State;
import amazing.generator.Generator;
import amazing.grid.Cell;
import amazing.grid.OverCell;
import amazing.grid.UnderCell;
import amazing.grid.WeaveGrid;

public class Display<O extends OverCell<O, U>, U extends UnderCell<U, O>, C extends Cell<C>, W extends WeaveGrid<O, U>> implements Callable<Void> {
    private ExecutorService exec;
    private GraphicsDevice gd;
    private State state;
    private int height, border, created;
    private Window screen;
    private Graphics2D g;
    private boolean fullscreen, messages;
    private Font font, msg;

    private final Object lock = new Object[0];

    public Display(GraphicsDevice gd, Frame root, ExecutorService exec, State state) {
        this.gd = gd;
        this.exec = exec;
        this.state = state;

        screen = new Window(root);
        screen.enableInputMethods(false);
        gd.setFullScreenWindow(screen);

        g = (Graphics2D) screen.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        fullscreen = fullscreen();
        messages = messages();
        font = font();
        msg = Font.decode(Constants.MSG_FONT);
        border = fullscreen ? 0 : (int) (font.getSize2D() / 2f);
        height = screen.getHeight() - (fullscreen ? 0 : (font.getSize() + ((messages ? 5 : 3) * border)));
        created = 0;
    }

    @SuppressWarnings("unchecked")
    public Void call() throws Exception {
        do {
            Generator<C> generator = (Generator<C>) sample(GENERATORS);
            int color = random(12);
            int rows = scale(random(20, 80));
            int columns = scale(random(40, 160));
            int size = scale(random(15, 30));
            float inset = choose(20) ? 0f : 0.1f + ratio() / 5f;
            boolean dark = choose(10);
            g.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, dark ? 250 : 100);

            state.reset();
            if (messages) {
                state.setStateChangedListener(() -> {
                    synchronized (lock) {
                        g.setFont(msg);
                        g.setColor(g.getBackground().equals(Color.BLACK) ? Color.WHITE : Color.BLACK);
                        String status = "";
                        for (String key : State.FLAGS) {
                            status += Character.toString(state.get(key) ? key.charAt(0) : ' ') + " ";
                        }
                        if (state.has(State.FILE)) {
                            status += String.format("%s ", state.file());
                        }
                        g.clearRect(border, border / 4, g.getFontMetrics().stringWidth(status), msg.getSize() + border / 4);
                        g.drawString(status, border, border / 4 + msg.getSize());
                    }
                });
            }

            Builder<O, U, C, W> task = new Builder<>(rows, columns, generator);
            Renderer<O, U, C, W> renderer = new Renderer<>(size, inset, color, dark);

            Instant start = Instant.now();
            Future<W> result = exec.submit(task);
            W grid = result.get();
            BufferedImage image = renderer.apply(grid);
            created++;

            AffineTransform transform = new AffineTransform();
            float scale = (float) screen.getWidth() / (float) image.getWidth();
            if ((int) (image.getHeight() * scale) > height) {
                scale *= (float) height / (float) (image.getHeight() * scale);
            }
            int iw = (int) (image.getWidth() * scale);
            int ih = (int) (image.getHeight() * scale);
            float x = 0f, y = 0f;
            if (iw < screen.getWidth()) {
                x = (screen.getWidth() - iw) / 2f;
            }
            if (ih < height) {
                y = (height - ih) / 2f;
            }
            transform.scale(scale, scale);
            transform.translate(x / scale, ((messages ? 3 : 1) * border + y) / scale);

            synchronized (lock) {
                g.setBackground(dark ? Color.BLACK : Color.WHITE);
                g.clearRect(0, 0, screen.getWidth(), screen.getHeight());

                if (!fullscreen) {
                    g.setColor(Color.GRAY);
                    g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                    g.drawLine(0, (int) ((messages ? 2.5f : 0.5f) * border),
                            screen.getWidth(), (int) ((messages ? 2.5f : 0.5f) * border));
                    g.drawLine(0, (int) ((messages ? 3.5f : 1.5f) * border) + height,
                            screen.getWidth(), (int) ((messages ? 3.5f : 1.5f) * border) + height);
                }
                g.drawImage(image, transform, null);

                String title = title(grid) + String.format(" / #%04d", created);
                if (!fullscreen) {
                    g.setFont(font);
                    g.setColor(dark ? Color.WHITE : Color.BLACK);

                    g.drawString(title, border, height + ((messages ? 4 : 2) * border) + (fullscreen ? -1 * (border + font.getSize()) : font.getSize()));
                    int bounds = g.getFontMetrics().stringWidth(ABOUT);
                    g.drawString(ABOUT, screen.getWidth() - border - bounds, height + ((messages ? 4 : 6) * border) + font.getSize());
                }
                if (DEBUG) System.out.printf("> %s\n", title);
            }

            if (DEBUG) {
                System.out.printf("- Image %d x %d / Scaled %d x %d / Scale %f / Offset %d, %d\n",
                        image.getWidth(), image.getHeight(), iw, ih, scale, (int) x, (int) y);
            }

            Duration length = Duration.between(start, Instant.now());
            if (DEBUG) System.out.printf("- Maze generation time %d ms\n", TimeUnit.MILLISECONDS.convert(length));

            sleep(pause());
            while (state.paused());

            if (zoom()) {
                long elapsed = 0l;
                int n = 0;
                long fs = random(5, 10);

                int steps = 10 * random(50, 500), total = 3 * steps;
                state.setSteps(total);

                float zoom = ((float) Math.PI / (choose() ? 2f : 4f)) / (2f * steps);
                float rotate = zoom * (choose() ? -1f : +1f);
                float move = Math.min(image.getWidth(), image.getHeight()) * (-0.0001f / steps);
                float magnify = (float) Math.pow(
                        (double) Math.max(rows, columns) / (Math.min(Math.min(rows, columns), 30d) * 0.667d),
                        1d / total);

                for (int z = 0; !state.skip() && z < total; z++) {
                    start = Instant.now();

                    if (z > 2 * steps) {
                        if (image.getWidth() > image.getHeight()) {
                            transform.translate(image.getWidth() * move, 0f);
                        } else {
                            transform.translate(0f, image.getHeight() * move);
                        }
                    }
                    if (z > steps) {
                        transform.rotate(rotate, image.getWidth() / 2f, image.getHeight() / 2f);
                    }
                    transform.translate(image.getWidth() / -2f, image.getHeight() / -2f);
                    transform.scale(magnify, magnify);
                    transform.translate(image.getWidth() / (magnify * magnify) / 2f, image.getHeight() / (magnify * magnify) / 2f);

                    synchronized (lock) {
                        g.setClip(0, (messages ? 3 : 1) * border, screen.getWidth(), height);

                        Point2D[] r = new Point2D[4];
                        r[0] = new Point2D.Float(-1f, -1f);
                        r[1] = new Point2D.Float(image.getWidth() + 2f, -1f);
                        r[2] = new Point2D.Float(image.getWidth() + 2f, image.getHeight() + 2f);
                        r[3] = new Point2D.Float(-1f, image.getHeight() + 2f);
                        transform.transform(r, 0, r, 0, 4);

                        g.setColor(g.getBackground());
                        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                        for (int p = 0; p < 4; p++) {
                            g.drawLine((int) r[p].getX(), (int) r[p].getY(), (int) r[(p + 1) % 4].getX(), (int) r[(p + 1) % 4].getY());
                        }

                        g.drawImage(image, transform, null);

                        g.setClip(0, 0, screen.getWidth(), screen.getHeight());

                        g.setFont(font);
                        g.setColor(dark ? Color.WHITE : Color.BLACK);

                        if (messages && (z % 10 == 0)) {
                            g.setFont(msg);
                            g.setColor(g.getBackground().equals(Color.BLACK) ? Color.WHITE : Color.BLACK);
                            int frames = g.getFontMetrics().stringWidth(" 000");
                            g.clearRect(screen.getWidth() - (border + frames), border / 4, frames, msg.getSize() + border / 4);
                            g.drawString(String.format("%4s", String.format("%03d", (z / 10) + 1)),
                                    screen.getWidth() - (border + frames), msg.getSize() + border / 4);
                        }
                    }

                    Duration frame = Duration.between(start, Instant.now());
                    elapsed += TimeUnit.MICROSECONDS.convert(frame);
                    n++;

                    sleep(fs);
                    while (state.paused());
                }

                if (zoom() && n > 0) {
                    if (DEBUG) {
                        System.out.printf("- Total processing time for %d frames: %d us\n", n, elapsed);
                        System.out.printf("- Average frame processing time: %d us\n", elapsed / total);
                    }
                }

                if (!state.skip()) sleep(minPause());
                while (state.paused());
            }
            while (state.waiting());

            if (state.saving()) {
                String file = save(image, Constants.PNG, saveDir(), "viewer");
                state.setFile(file);
                if (DEBUG) System.out.printf("+ Saved image as %s\n", file);
                sleep(2);
            }
        } while (!state.quitting());

        gd.setFullScreenWindow(null);
        return (Void) null;
    }

    public static Frame frame(GraphicsDevice gd) {
        Frame root = new Frame(gd.getDefaultConfiguration());
        root.setName(Constants.VERSION);
        try (InputStream icon = Display.class.getResourceAsStream("/icon.png")) {
            root.setIconImage(ImageIO.read(icon));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        root.enableInputMethods(true);
        root.setVisible(true);
        return root;
    }

    public static List<GraphicsDevice> monitors() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        List<GraphicsDevice> gds = Arrays.asList(ge.getScreenDevices());
        return gds;
    }
}