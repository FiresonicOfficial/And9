# PiePlay 32 - Android 9 (Pie) 32-Bit Emulator Sandbox

A native Android 9 (Pie) 32-bit (armeabi-v7a) gaming emulator sandbox with hardware acceleration, virtual gamepad controls, custom APK sideloading, and retro arcade titles.

---

## 🚀 GitHub Actions: Build & Extract APK Workflow

This repository includes an automated GitHub Actions CI/CD workflow located at [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml) that compiles the project, extracts the APK files, generates SHA256 checksums, and makes them available for instant download.

### How to Extract the APK from GitHub:

#### Option 1: Automatic Build on Push / Pull Request
1. Every time you push to `main` or open a PR, the workflow automatically runs.
2. Go to the **Actions** tab in your GitHub repository.
3. Click on the latest run titled **"Build & Extract Android APK"**.
4. Scroll down to the **Artifacts** section at the bottom of the summary page.
5. Download **`PiePlay32-APK-Artifacts.zip`**, which contains:
   - `PiePlay32-v1.0-debug.apk` (Signed with debug key, ready to install directly on your device)
   - `SHA256SUMS.txt` (Integrity checksums)

#### Option 2: Manual Trigger (Workflow Dispatch)
1. Navigate to the **Actions** tab in GitHub.
2. Under "Workflows" on the left, select **"Build & Extract Android APK"**.
3. Click **"Run workflow"** on the right side.
4. Choose the branch and build type:
   - `debug` (default, pre-signed and ready to install)
   - `release`
   - `both`
5. (Optional) Check "Publish APK(s) to GitHub Releases" if you want to create a release.
6. Click **"Run workflow"**. When finished, download the APK from the artifacts table.

#### Option 3: Creating a GitHub Release with APK
Tag your commit and push it to GitHub:
```bash
git tag v1.0
git push origin v1.0
```
The workflow will automatically create a GitHub Release with the extracted `.apk` files attached as release assets.

---

## 🛠 Features

- **APK Sideloading**: Direct `.apk` file picker to install 32-bit apps and games.
- **Hardware Acceleration**: Simulated OpenGL ES 3.2 / Vulkan low-overhead graphics pipeline.
- **ARMv7-A 32-Bit Compatibility**: Dynamic instruction translator with NEON SIMD support.
- **Customizable Virtual Controller**: Floating D-Pad, A/B/X/Y action buttons, L/R bumpers, and physical keyboard support.
- **Save States & Telemetry**: Live FPS counter, chip synthesizer retro audio, and save state persistence via Room.
