# Native Packaging Guide

This guide explains how to build and distribute native installers for the Java WebView application on Windows, macOS, and Linux.

## Overview

The application supports multiple distribution formats:

- **Windows**: MSI installer
- **macOS**: DMG disk image with app bundle
- **Linux**: DEB (Debian/Ubuntu) and RPM (Red Hat/Fedora) packages

## Prerequisites

### All Platforms
- Java 17 or higher
- Maven 3.6+
- Git

### Windows
- Windows 10 or later
- WiX Toolset (automatically handled by jpackage)

### macOS
- macOS 10.14 or later
- Xcode command line tools

### Linux
- Ubuntu/Debian or Red Hat/Fedora
- rpm-build package (for RPM creation)

## Building Packages

### Quick Start

#### Windows MSI
```cmd
# Build MSI installer
build-windows.bat

# Or manually:
mvn clean package -P windows
mvn jpackage:jpackage -P windows
```

#### macOS DMG
```bash
# Build DMG installer
./build-macos.sh

# Or manually:
mvn clean package -P macos
mvn jpackage:jpackage -P macos
```

#### Linux DEB
```bash
# Build DEB package
./build-deb.sh

# Or manually:
mvn clean package -P linux-deb
mvn jpackage:jpackage -P linux-deb
```

#### Linux RPM
```bash
# Build RPM package
./build-rpm.sh

# Or manually:
mvn clean package -P linux-rpm
mvn jpackage:jpackage -P linux-rpm
```

### Build Output

Packages are created in `target/dist/` directory:

```
target/dist/
├── java-webview-app-1.3.0.msi      # Windows
├── java-webview-app-1.3.0.dmg      # macOS
├── java-webview-app_1.3.0_amd64.deb # Linux DEB
└── java-webview-app-1.3.0-1.x86_64.rpm # Linux RPM
```

## Installation

### Windows
1. Download the `.msi` file
2. Double-click to run the installer
3. Follow the installation wizard
4. Launch from Start Menu or Desktop shortcut

### macOS
1. Download the `.dmg` file
2. Double-click to mount the disk image
3. Drag the app to Applications folder
4. Launch from Applications or Dock

### Linux (DEB)
```bash
# Using dpkg
sudo dpkg -i java-webview-app_1.3.0_amd64.deb

# Or using apt
sudo apt install ./java-webview-app_1.3.0_amd64.deb
```

### Linux (RPM)
```bash
# Using rpm
sudo rpm -i java-webview-app-1.3.0-1.x86_64.rpm

# Or using dnf/yum
sudo dnf install java-webview-app-1.3.0-1.x86_64.rpm
```

## Customization

### Application Metadata

Edit `package/build.properties` to customize:

```properties
app.name=Java WebView App
app.version=1.3.0
app.vendor=Your Company
app.description=Your application description
```

### Icons

Place platform-specific icons in the `package/` directory:

- `app-icon.ico` - Windows icon (256x256 recommended)
- `app-icon.icns` - macOS icon (512x512 recommended)
- `app-icon.png` - Linux icon (512x512 recommended)

### Build Configuration

Modify `pom.xml` profiles to customize packaging:

```xml
<plugin>
    <groupId>org.panteleyev</groupId>
    <artifactId>jpackage-maven-plugin</artifactId>
    <configuration>
        <!-- Custom configuration options -->
        <name>Your App Name</name>
        <winMenuGroup>Your Company</winMenuGroup>
        <linuxMenuGroup>Development</linuxMenuGroup>
    </configuration>
</plugin>
```

## Advanced Features

### Code Signing

#### Windows
```xml
<configuration>
    <winConsole>true</winConsole>
    <signBundle>true</signBundle>
    <vendor>Your Company</vendor>
</configuration>
```

#### macOS
```xml
<configuration>
    <macSign>true</macSign>
    <macPackageName>com.yourcompany.yourapp</macPackageName>
</configuration>
```

### Custom Runtime

Create custom JRE for smaller packages:

```xml
<configuration>
    <runtimeImage>${project.build.directory}/runtime</runtimeImage>
    <javaOptions>
        <javaOption>-Djava.awt.headless=false</javaOption>
    </javaOptions>
</configuration>
```

## Troubleshooting

### Common Issues

#### "jpackage not found"
- Ensure Java 14+ is installed
- jpackage is included with JDK since Java 14

#### "Icon file not found"
- Check that icon files exist in `package/` directory
- Ensure correct file formats (.ico, .icns, .png)

#### "Permission denied" (Linux)
- Run build scripts with proper permissions
- Use `sudo` for system-wide installation

#### "Code signing failed"
- Obtain proper code signing certificates
- Configure signing in pom.xml profiles

### Build Logs

Enable verbose logging:

```bash
mvn jpackage:jpackage -Dmaven.jpackage.verbose=true
```

### Testing Packages

#### Windows
- Test MSI installation on clean VM
- Verify shortcuts and uninstaller

#### macOS
- Test DMG mounting and installation
- Verify Gatekeeper acceptance

#### Linux
- Test package installation on target distributions
- Verify desktop integration

## Distribution

### GitHub Releases

1. Build packages for all platforms
2. Create GitHub release
3. Upload all package files
4. Add release notes

### Enterprise Distribution

For enterprise deployment:

- Use `--win-dir-chooser` for custom install paths
- Enable silent installation with `/quiet` (Windows)
- Use package managers for Linux deployment

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build Packages
on: [push, pull_request]

jobs:
  build:
    runs-on: ${{ matrix.os }}
    strategy:
      matrix:
        os: [windows-latest, macos-latest, ubuntu-latest]

    steps:
    - uses: actions/checkout@v3
    - uses: actions/setup-java@v3
      with:
        java-version: 17
        distribution: temurin
    - name: Build package
      run: |
        if [ "$RUNNER_OS" == "Windows" ]; then
          ./build-windows.bat
        elif [ "$RUNNER_OS" == "macOS" ]; then
          ./build-macos.sh
        else
          ./build-deb.sh
        fi
    - uses: actions/upload-artifact@v3
      with:
        name: packages-${{ matrix.os }}
        path: target/dist/
```

## Support

For packaging issues:
- Check jpackage documentation
- Verify Java version compatibility
- Test on target platforms

## Version History

- **1.3.0**: Initial native packaging support
- Added MSI, DMG, DEB, and RPM creation
- Cross-platform build scripts
- Code signing support