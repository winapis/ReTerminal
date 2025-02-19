# ReTerminal
**ReTerminal** is a sleek, Material 3-inspired terminal emulator designed as a modern alternative to the legacy [Jackpal Terminal](https://github.com/jackpal/Android-Terminal-Emulator). Built on [Termux's](https://github.com/termux/termux-app) robust TerminalView

# Features
- [x] Basic Terminal
- [x] Virtual Keys
- [x] Multiple Sessions

# Screenshots
<div>
  <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/01.jpg" width="32%" />
  <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/02.jpg" width="32%" />
  <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/03.jpg" width="32%" />
</div>


# FAQ

**Q: Why there's no commands like git wget etc.**

**A: ReTerminal is not meant for that use Termux instead**


**Q: Why do I get a "Permission Denied" error when trying to execute a binary or script?**  

**A:** ReTerminal targets the latest Android API, which enforces W^X restrictions, preventing direct execution of files. Here are some ways to work around this:

### 1. Use the Dynamic Linker  
For binaries, you can use the dynamic linker to execute them:

```bash
$LINKER /absolute/path/to/binary
```

Note: This method does not work with static binaries.

2. Use sh for Scripts

If the file is a script, execute it with sh:

```bash
sh path/to/script
```

3. Use Shizuku for Shell Access

If you have Shizuku installed, you can execute files stored in /data/local/tmp with shell access.

