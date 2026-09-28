# Java Ray Tracer

This project is a small ray tracer written in Java. It was created during the Game Technology & Interactive Systems course at HTW Berlin.

The program generates an image by sending a ray from the camera through every pixel and checking if it intersects with an object in the scene.

## Features

- camera and per-pixel ray generation
- ray-sphere intersection
- multiple spheres in one scene
- nearest object detection
- point lighting
- Lambert shading
- image output as PNG

## How it works

For every pixel, a ray is created from the camera into the scene.

The program checks the ray against all spheres and calculates whether an intersection occurs. If there are multiple intersections, the closest visible object is used.

The final pixel color is calculated using the surface normal and the position of the light source.

## Technologies

- Java
- Computer Graphics
- Ray Tracing
- Vector Mathematics

## Context

Created as part of the Game Technology & Interactive Systems course at HTW Berlin.
