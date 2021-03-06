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

import static amazing.Constants.COPYRIGHT;
import static amazing.Constants.WATERMARK_FONT;
import static amazing.Constants.watermark;
import static amazing.Utils.color;
import static amazing.Utils.ratio;
import static amazing.Constants.Colors;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.util.function.Function;

import amazing.grid.Cell;
import amazing.grid.OverCell;
import amazing.grid.UnderCell;
import amazing.grid.WeaveGrid;

/**
 * Renders a maze {@link Grid grid} as an {@link BufferedImage image} with
 * the specified sizes and color.
 */
public class Renderer<O extends OverCell<O,U>, U extends UnderCell<U,O>, C extends Cell<C>, W extends WeaveGrid<O,U>> implements Function<W,BufferedImage> {
    private int size, color;
    private float inset;
    private boolean dark;

    public Renderer(int size, float inset, int color, boolean dark) {
        this.size = size;
        this.inset = inset;
        this.color = color;
        this.dark = dark;
    }

    public Color getBackground(W grid, O cell) {
        if (grid.getDistances().isPresent() && grid.getDistances().get().isSet(cell)) {
            int distance = grid.getDistances().get().getDistance(cell);
            float intensity = (float) (grid.getMaximum() - distance) / grid.getMaximum();
            int dark = (int) (255 * intensity);
            int light = 255 - dark;
            int bright = 128 + (int) (127 * intensity);

            switch (color) {
                case Colors.RED:            return color(bright, dark, dark);
                case Colors.GREEN:          return color(dark, bright, dark);
                case Colors.BLUE:           return color(dark, dark, bright);
                case Colors.CYAN:           return color(dark, bright, bright);
                case Colors.MAGENTA:        return color(bright, dark, bright);
                case Colors.YELLOW:         return color(bright, bright, dark);
                case Colors.MAGENTA_CYAN:   return color(light, dark, bright);
                case Colors.YELLOW_MAGENTA: return color(bright, light, dark);
                case Colors.CYAN_YELLOW:    return color(dark, bright, light);
                case Colors.CYAN_RED:       return color(dark, light, light);
                case Colors.MAGENTA_GREEN:  return color(light, dark, light);
                case Colors.YELLOW_BLUE:    return color(light, light, dark);
                default:                    return color(dark, dark, dark);
            }
        } else return Color.WHITE;
    }

    private int[] coordinates(int x, int y, int size, int inset) {
        int x1 = x;
        int x4 = x + size;
        int x2 = x1 + inset;
        int x3 = x4 - inset;

        int y1 = y;
        int y4 = y + size;
        int y2 = y1 + inset;
        int y3 = y4 - inset;

        return new int[] { x1, x2, x3, x4, y1, y2, y3, y4 };
    }

    public BufferedImage apply(W grid) {
        int w = 1 + (size * grid.getColumns());
        int h = 1 + (size * grid.getRows());
        int i = (int) (size * inset);

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setBackground(dark ? Color.BLACK : Color.WHITE);
        g.setStroke(new BasicStroke(0.5f + ratio(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.clearRect(0, 0, w, h);

        for (O cell : grid) {
            int x = cell.getColumn() * size;
            int y = cell.getRow() * size;
            int[] xy = coordinates(x, y, size, i);
            int x1 = xy[0];
            int x2 = xy[1];
            int x3 = xy[2];
            int x4 = xy[3];
            int y1 = xy[4];
            int y2 = xy[5];
            int y3 = xy[6];
            int y4 = xy[7];

            int in = x3 - x2;
            int out = x2 - x1;

            if (!cell.hasLinks()) continue;

            Color bg = getBackground(grid, cell);
            g.setColor(bg);
            g.fillRect(x2, y2, in, in);

            if (cell.linked(cell.getNorth())) {
                if (i > 0) {
                    g.setColor(bg);
                    g.fillRect(x2, y1, in, out);
                    g.setColor(Color.BLACK);
                    g.drawLine(x2, y1, x2, y2);
                    g.drawLine(x3, y1, x3, y2);
                }
            } else {
                g.setColor(Color.BLACK);
                g.drawLine(x2, y2, x3, y2);
            }
            if (cell.linked(cell.getSouth())) {
                if (i > 0) {
                    g.setColor(bg);
                    g.fillRect(x2, y3, in, out);
                    g.setColor(Color.BLACK);
                    g.drawLine(x2, y3, x2, y4);
                    g.drawLine(x3, y3, x3, y4);
                }
            } else {
                g.setColor(Color.BLACK);
                g.drawLine(x2, y3, x3, y3);
            }
            if (cell.linked(cell.getWest())) {
                if (i > 0) {
                    g.setColor(bg);
                    g.fillRect(x1, y2, out, in);
                    g.setColor(Color.BLACK);
                    g.drawLine(x1, y2, x2, y2);
                    g.drawLine(x1, y3, x2, y3);
                }
            } else {
                g.setColor(Color.BLACK);
                g.drawLine(x2, y2, x2, y3);
            }
            if (cell.linked(cell.getEast())) {
                if (i > 0) {
                    g.setColor(bg);
                    g.fillRect(x3, y2, out, in);
                    g.setColor(Color.BLACK);
                    g.drawLine(x3, y2, x4, y2);
                    g.drawLine(x3, y3, x4, y3);
                }
            } else {
                g.setColor(Color.BLACK);
                g.drawLine(x3, y2, x3, y3);
            }
        }

        if (watermark()) {
            Font watermark = Font.decode(WATERMARK_FONT);
            watermark = watermark.deriveFont(watermark.getStyle(), (1f - inset) * size * 0.5f);
            g.setFont(watermark);
            g.setColor(Color.GRAY);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));

            int bounds = g.getFontMetrics().stringWidth(COPYRIGHT);
            g.drawString(COPYRIGHT, w - (size / 3) - bounds - (inset * size * 0.5f), h - (size / 3) - (inset * size * 0.5f));
        }

        return image;
    }
}