# Quick Start Guide - ADC

This guide covers running the CLI with **Application Default Credentials
(ADC)** instead of a service account key file.

## 1. Go to `bin`

Windows 🪟:

```cmd
cd bin
```

Linux/macOS 🍎:

```sh
cd bin
```

## 2. Authenticate with gcloud

No key file is needed. Instead, log in with the Google Cloud SDK:

```sh
gcloud auth application-default login
```

This stores local ADC credentials that the CLI will use automatically when
`--adc` is passed.

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

## 5. Set your project

Open `test-all-eu-adc.cmd` and set `PROJECT` to your Google Cloud project ID:

```cmd
set PROJECT=your-gcp-project-id
```

## 6. Run a test

Windows:

```cmd
test-all-eu-adc.cmd
```

This calls the CLI with `--adc --project <PROJECT>`, using ADC credentials
instead of a service account key file:

```text
java -jar vertex-latest.jar --adc --project <PROJECT> --check-all-regions --cluster EU -model-file models.properties --text "..."
```

## 7. Check results

Test output files are written under:

```text
bin/results
```

## Single-request example

```sh
vertex-ai --adc --project my-gcp-project --adc-location europe-west1 -m gemini.pro "Hello"
```

`--adc-location` is only required in normal (single-request) mode; it is
optional with `--check-all-regions` / `--worldwide`, which resolve a location
automatically.
