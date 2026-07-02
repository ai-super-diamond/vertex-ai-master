# Quick Start Guide

## 1. Go to `bin`

Windows:

```cmd
cd bin
```

Linux/macOS:

```sh
cd bin
```

## 2. Copy service account key

Copy your Google service account JSON key to:

Windows:

```text
..\keys\sa_key.json
```

Linux/macOS:

```text
../keys/sa_key.json
```

The CLI reads the Google Cloud project ID from the key file's `project_id` field.

## 3. Run doctor

Windows:

```cmd
doctor.cmd
```

Linux/macOS:

```sh
./doctor.sh
```

## 4. Build the JAR

Windows:

```cmd
build-jar.cmd
```

Linux/macOS:

```sh
./build-jar.sh
```

This creates:

```text
bin/vertex-latest.jar
```

## 5. Run a test

Windows:

```cmd
test-worldwide.cmd
```

Linux/macOS:

```sh
./test-worldwide.sh
```

## 6. Check results

Test output files are written under:

```text
bin/results
```
