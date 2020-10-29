Amazing Maze Generator 0.9
==========================

Introduction.

# About

TODO.

## Features

TODO.

# Code

Java 8 or better, with no extra libraries, using AWT for display.

## Build

No build system, uses VS Code IDE.

No version control.

## Structure

### Package `amazing.generator`

### Package `amazing.grid`

### Package `amazing.task`

### Package `amazing`

### Applications

#### Mazes

#### Viewer

#### Multi

## Algorithms

TODO.

## Tasks

TODO.

### Contributiong

TODO.

# Usage

## Java Commands and Arguments

```shell
$ java [...] [-Damazing.*] Mazes [n [filename [generator]]]
$ java [...] [-Damazing.*] [-Damazing.display.*] Viewer [monitor]
$ java [...] [-Damazing.*] [-Damazing.display.*] Multi
```

## Display Keyboard Functions

_Multi_ and _Viewer_
- `Q` Quit after displaying this maze
- `_` Pause the zoom

_Viewer_ only
- `S` Save this maze as an image
- `N` Skip to the next maze immediately
- `W` Wait indefinitely after displaying 

## System properties

### Global runtime configuration

- `amazing.debug`
- `amazing.seed`
- `amazing.watermark`
- `amazing.scale`
- `amazing.save.dir`
- `amazing.save.format`

### Display configuration

- `amazing.display.pause.min`
- `amazing.display.pause.max`
- `amazing.display.font`
- `amazing.display.zoom`
- `amazing.display.fullscreen`

# References

TODO

---
Copyright 2020 by Andrew Donald Kennedy;
Licensed as [APACHE-2.0](http://www.apache.org/licenses/LICENSE-2.0)