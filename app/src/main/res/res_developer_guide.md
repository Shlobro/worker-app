# Resources Developer Guide

This folder contains Android resource directories used by the app at runtime.

## Subfolders

- `values/`: Default string resources.
- `values-en/`: English string resources.
- `values-he/`: Hebrew string resources.
- `values-he-rIL/`: Hebrew (Israel) string resources.

## Usage Notes

- Keep the same string keys across locale folders.
- When adding new UI text, add it to all supported locale folders in this project.
- Use descriptive comments in `strings.xml` blocks to group related keys.
- Do not place `.md` files inside `values*` folders; Android resource merging only accepts supported resource file types there.
