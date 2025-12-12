# Messages Customization

As of version 1.2.0, **every message** in SimpleTrains can be customized through `messages.yml`.

---

## Overview

The `messages.yml` file contains:
- All command feedback messages
- Error messages
- GUI titles and item names
- Lore text
- Help menu text

---

## File Location

```
plugins/SimpleTrains/messages.yml
```

The file is generated automatically on first run.

---

## Color Codes

Use `&` followed by a color/format code:

| Code | Color | Code | Color |
|------|-------|------|-------|
| `&0` | Black | `&8` | Dark Gray |
| `&1` | Dark Blue | `&9` | Blue |
| `&2` | Dark Green | `&a` | Green |
| `&3` | Dark Aqua | `&b` | Aqua |
| `&4` | Dark Red | `&c` | Red |
| `&5` | Dark Purple | `&d` | Light Purple |
| `&6` | Gold | `&e` | Yellow |
| `&7` | Gray | `&f` | White |

| Code | Format |
|------|--------|
| `&l` | **Bold** |
| `&o` | *Italic* |
| `&n` | <u>Underline</u> |
| `&m` | ~~Strikethrough~~ |
| `&k` | Obfuscated |
| `&r` | Reset |

**Examples:**
```yaml
prefix: "&6[SimpleTrains] &r"
station-created: "&aStation created!"
error: "&c&lError: &cSomething went wrong"
```

---

## Placeholders

Placeholders are replaced with actual values. Format: `%PLACEHOLDER%`

| Placeholder | Description | Used In |
|-------------|-------------|---------|
| `%STATION%` | Station name | Most messages |
| `%STATION_A%` | First station in link | Link messages |
| `%STATION_B%` | Second station in link | Link messages |
| `%PLAYER%` | Player name | Transfer, requests |
| `%COST%` | XP cost | Creation, linking |
| `%CURRENT%` | Current XP level | XP error |
| `%DESTINATION%` | Destination station | Warp messages |
| `%DISTANCE%` | Distance in blocks | Near command |
| `%BLOCK%` | Block material name | Creation messages |
| `%MATERIAL%` | Material name | Block command |
| `%MESSAGE%` | Station welcome message | Welcome messages |
| `%OWNER%` | Station owner name | GUI lore |
| `%COUNT%` | Number count | Link counts |
| `%X%`, `%Z%` | Coordinates | Location display |
| `%TYPE%` | Link type | Link messages |
| `%INITIATOR%` | Request initiator | Link requests |
| `%INITIATING_STATION%` | Initiating station | Link requests |
| `%RECEIVING_STATION%` | Receiving station | Link requests |

---

## Message Categories

### Prefix
```yaml
prefix: "&6[SimpleTrains] &r"
```
Added before most messages. Change color or text as desired.

---

### Command Messages

```yaml
# Console error
console-only-players: "&cOnly players can use train commands."

# Unknown command
unknown-command: "&cUnknown command. Use /train help."
```

---

### Station Creation/Deletion

```yaml
# Usage hints
set-usage: "&cUsage: /train set <stationName>"
delete-usage: "&cUsage: /train delete <stationName>"

# Success messages
station-created: "&aStation '%STATION%' created successfully! (-%COST% XP)"
station-deleted: "&aStation '%STATION%' and all associated links deleted."

# Error messages
station-already-exists: "&cStation '%STATION%' already exists!"
station-not-found: "&cStation '%STATION%' does not exist."
not-enough-xp-create: "&cYou need %COST% XP levels to create a station. You currently have %CURRENT%."
wrong-block-position: "&cYou must be standing on a RAIL block, with a %BLOCK% block directly beneath it."
```

---

### Linking Messages

```yaml
# Link success
stations-linked: "&aStations '%STATION_A%' and '%STATION_B%' linked successfully as %TYPE%!"
stations-unlinked: "&aStations '%STATION_A%' and '%STATION_B%' unlinked."

# Link requests
link-request-sent: "&eLink request sent for '%STATION_A%' to owner of '%STATION_B%'. Link Type: %TYPE%."
link-request-received: "&bLink request received from %PLAYER% to link your station '%STATION_B%' with '%STATION_A%'."
link-accepted: "&aLink to '%STATION%' accepted! Stations are now linked as PUBLIC."
link-rejected: "&cLink request from '%STATION%' rejected."
```

---

### GUI Titles

```yaml
gui-title-station-list: "&3Station List"
gui-title-my-stations: "&eMy Stations"
gui-title-all-stations: "&eAll Stations"
gui-title-select-destination: "&3Select Destination"
gui-title-station-config: "&4Station Configuration"
gui-title-link-manager: "&3Link Manager"
gui-title-pending-requests: "&6Pending Link Requests"
```

---

### GUI Item Names

```yaml
gui-item-previous-page: "&e<< Previous Page"
gui-item-next-page: "&eNext Page >>"
gui-item-switch-my-stations: "&dSwitch to My Stations"
gui-item-switch-all-stations: "&dSwitch to All Stations"
gui-item-edit-message: "&eEdit Welcome Message"
gui-item-link-manager: "&bLink Manager"
gui-item-delete-station: "&cDELETE STATION"
gui-item-back-main: "&fBack to Main List"
```

---

### GUI Lore (Item Descriptions)

```yaml
gui-lore-owner: "&7Owner: &f%OWNER%"
gui-lore-linked-stations: "&7Linked Stations: &f%COUNT%"
gui-lore-location: "&7Location: &f%X%, %Z%"
gui-lore-click-configure: "&5Click to open configuration (if owned)"
gui-lore-delete-warning: "&4Warning: This action is permanent."
gui-lore-link-type-public: "&a[PUBLIC]"
gui-lore-link-type-private: "&c[PRIVATE]"
```

---

### Help Menu

```yaml
help-header: "&8--- SimpleTrains Help ---"
help-set: "&e/train set <name> &7-> Creates a station (-%COST% XP)."
help-delete: "&e/train delete <name> &7-> Deletes a station."
help-link: "&e/train link <A> <B> [type] &7-> Sends link request (Type: public/private)."
help-transfer: "&e/train transfer <st> <player> &7-> Transfers ownership (Owner/Admin)."
help-near: "&e/train near &7-> Lists nearby stations."
help-gui: "&e/train gui &7-> Opens the station list GUI."
help-accept-reject: "&e/train accept/reject <A> <B> &7-> Accept/Reject a link request."
help-message: "&e/train message <st> <&msg> &7-> Sets station welcome message."
help-block: "&e/train block <mat> &7-> (Admin) Sets creation block."
```

---

## Examples

### Spanish Translation

```yaml
prefix: "&6[SimpleTrains] &r"
station-created: "&aEstación '%STATION%' creada exitosamente! (-%COST% XP)"
station-deleted: "&aEstación '%STATION%' eliminada."
station-not-found: "&cLa estación '%STATION%' no existe."
no-permission: "&cNo tienes permiso para esto."
help-header: "&8--- Ayuda de SimpleTrains ---"
```

### Minimalist Style

```yaml
prefix: "&8[&bST&8] "
station-created: "&7Station &f%STATION% &7created"
station-deleted: "&7Station &f%STATION% &7deleted"
no-permission: "&cNo permission"
```

### Colorful/Fun Style

```yaml
prefix: "&d&l★ &5SimpleTrains &d&l★ &r"
station-created: "&a&l✓ &aStation &e%STATION% &acreated! &7(-%COST% XP)"
station-deleted: "&c&l✗ &cStation &e%STATION% &cdeleted!"
warp-confirm: "&b&l➤ &bTeleporting to &e%DESTINATION%&b..."
```

---

## Tips

1. **Always test** your changes in-game after editing
2. **Keep placeholders** - removing them may cause errors
3. **Use quotes** around messages with special characters
4. **Backup** before making major changes
5. **Reset a message** by deleting its line (defaults will be used)

---

## Resetting Messages

To reset all messages to default:
1. Stop the server
2. Delete `messages.yml`
3. Start the server (new file will be generated)

---

## See Also

- [Configuration](Configuration) - Main plugin settings
- [GUI Guide](GUI-Guide) - Understanding the GUI
