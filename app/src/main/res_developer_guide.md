# Resources Developer Guide

Covers `res/` and all its subfolders. Android resource directories cannot contain `.md` files, so this guide lives one level up.

## Folder Structure

- **res/drawable/**: App icons and vector drawables.
- **res/mipmap-\*/**: Launcher icons at various densities.
- **res/xml/**: App configuration XML (e.g., locale config).

### Localization (res/values\*)

The app's default locale is Hebrew. Four locale folders contain string translations:

| Folder | Locale | Coverage |
|---|---|---|
| `res/values/` | Default (Hebrew) | Full — all keys live here |
| `res/values-en/` | English | Full translation |
| `res/values-he/` | Hebrew | Full translation |
| `res/values-he-rIL/` | Hebrew (Israel) | Partial — subset of keys |

**When adding a new string resource:**
1. Add the key to `values/strings.xml` (default/Hebrew)
2. Add the English translation to `values-en/strings.xml`
3. Add the Hebrew translation to `values-he/strings.xml`
4. Optionally add to `values-he-rIL/strings.xml`

Lint may report `MissingTranslation` when locale folders have inconsistent key coverage, depending on project lint configuration.

Use `%1$s`, `%1$d`, `%1$.2f` placeholders for formatted strings.

### res/values/ Files

- **strings.xml**: All app string resources in Hebrew, organized by section (navigation, dashboard, projects, workers, shifts, events, payments, common, validation, employers, reference payments).
- **colors.xml**: Color definitions (mostly unused template defaults).
- **themes.xml**: Material3 theme using dynamic color.

### Locale Note

Android maps `he` to `iw` internally. Lint warns about this (`LocaleFolder`), but the folders work correctly on modern Android.
