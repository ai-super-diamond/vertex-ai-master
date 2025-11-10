# Code Formatting Toolchain

This project uses automated code formatting and style checking to maintain consistency.

## Tools Configured

### 1. **Spotless (Auto-formatting)**
Automatically formats Java code and pom.xml files according to Eclipse formatter rules.

**Commands:**
```bash
# Check if code is formatted (runs on validate phase automatically)
mvn spotless:check

# Auto-format all code
mvn spotless:apply
```

**What it does:**
- Formats Java code with Eclipse formatter (2-space indentation)
- Removes unused imports
- Trims trailing whitespace
- Ensures files end with newline
- Sorts pom.xml elements consistently

### 2. **Checkstyle (Style Enforcement)**
Validates code against Google Java Style guidelines.

**Commands:**
```bash
# Run checkstyle manually
mvn checkstyle:check

# Generate checkstyle report
mvn checkstyle:checkstyle
```

**Configuration:**
- Uses Google checks (google_checks.xml)
- Runs on verify phase
- Currently set to warn (failsOnError=false) but violations are logged

### 3. **SpotBugs (Static Analysis)**
Detects potential defects such as null-pointer dereferences and threading issues.

**Commands:**
```bash
# Run static analysis (requires a SpotBugs-compatible JDK, e.g., 21 or 22)
mvn -Pspotbugs verify
```

**Notes:**
- SpotBugs runs in an optional Maven profile so day-to-day builds remain fast.
- When using JDK preview builds (e.g., Java 23+), SpotBugs currently fails to parse some JDK classes; switch to a supported LTS JDK before running the profile.

### 3. **EditorConfig**
Ensures consistent editor settings across different IDEs.

**Configured settings:**
- UTF-8 encoding
- LF line endings
- 2-space indentation for Java, XML, properties
- Insert final newline
- Trim trailing whitespace

## Workflow

### Before Committing
```bash
# Format all code
mvn spotless:apply

# Verify everything compiles and tests pass
mvn clean test

# Optional: run static analysis (requires supported JDK)
mvn -Pspotbugs verify
```

### CI/CD Integration
The build automatically:
1. Checks formatting (spotless:check) on validate phase
2. Compiles code
3. Runs tests
4. Runs checkstyle on verify phase (optional)

### IDE Setup

**IntelliJ IDEA:**
- Install "EditorConfig" plugin (usually pre-installed)
- Settings → Editor → Code Style → Import eclipse-formatter.xml

**VS Code:**
- Install "EditorConfig for VS Code" extension
- Install "Language Support for Java" extension

**Eclipse:**
- EditorConfig support built-in
- Import eclipse-formatter.xml directly

## Configuration Files

- `.editorconfig` - Cross-editor settings
- `eclipse-formatter.xml` - Java code formatting rules
- `pom.xml` - Maven plugin configurations (Spotless, Checkstyle)

## Pre-commit Hook (Optional)

Create `.git/hooks/pre-commit`:
```bash
#!/bin/sh
mvn spotless:apply
git add -u
```

Make it executable:
```bash
chmod +x .git/hooks/pre-commit
```
