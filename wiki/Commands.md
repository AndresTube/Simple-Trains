# Commands

All SimpleTrains commands start with `/train`. The alias `/sttrain` also works.

---

## Command Overview

| Command | Description | Who Can Use |
|---------|-------------|-------------|
| `/train help` | Show help menu | Everyone |
| `/train gui` | Open station GUI | Everyone |
| `/train near` | List nearby stations | Everyone |
| `/train set <name>` | Create a station | Players with permission |
| `/train delete <name>` | Delete a station | Owner / Admin |
| `/train link <A> <B> [type]` | Link two stations | Owner of A |
| `/train unlink <A> <B>` | Remove a link | Owner of either |
| `/train accept <A> <B>` | Accept link request | Owner of B |
| `/train reject <A> <B>` | Reject link request | Owner of B |
| `/train transfer <station> <player>` | Transfer ownership | Owner / Admin |
| `/train message <station> <msg>` | Set welcome message | Owner / Admin |
| `/train block <material>` | Set creation block | Admin only |
| `/train reload` | Reload configuration | Admin only |

---

## Detailed Command Reference

### `/train help`
Shows the help menu with all available commands.

**Usage:** `/train help`

---

### `/train gui`
Opens the station management GUI where you can:
- View all stations or just your stations
- Open station configuration
- Manage links

**Usage:** `/train gui`

---

### `/train near`
Lists all stations within 500 blocks of your current position, sorted by distance.

**Usage:** `/train near`

**Example output:**
```
--- Stations Near You (Max 500m) ---
CentralStation (45m)
MarketPlace (120m)
Harbor (340m)
```

---

### `/train set <name>`
Creates a new station at your location.

**Requirements:**
- Standing on a rail block
- Rail must be on top of the creation block (default: Gold Block)
- Enough XP levels (default: 5)

**Usage:** `/train set <stationName>`

**Examples:**
```
/train set CentralStation
/train set Market_Place
/train set Harbor123
```

---

### `/train delete <name>`
Permanently deletes a station and all its links.

**Requirements:**
- Must be station owner OR have admin permission

**Usage:** `/train delete <stationName>`

**Example:**
```
/train delete OldStation
```

> **Warning:** This action cannot be undone!

---

### `/train link <A> <B> [type]`
Creates a link between two stations.

**Requirements:**
- Must own station A
- Station B must exist
- Not already linked

**Link Types:**
- `public` (default) - Anyone can travel
- `private` - Only owners can travel

**Usage:** `/train link <stationA> <stationB> [public|private]`

**Examples:**
```
/train link CentralStation Harbor
/train link CentralStation Harbor public
/train link MyBase SecretRoom private
```

**Note:** If you don't own station B, a request is sent to the owner.

---

### `/train unlink <A> <B>`
Removes the link between two stations.

**Requirements:**
- Must own station A OR station B
- OR have admin permission

**Usage:** `/train unlink <stationA> <stationB>`

**Example:**
```
/train unlink CentralStation Harbor
```

---

### `/train accept <A> <B>`
Accepts a pending link request.

**Requirements:**
- Must own station B (the receiving station)
- A pending request must exist

**Usage:** `/train accept <initiatingStation> <yourStation>`

**Example:**
```
/train accept PlayerStation MyStation
```

---

### `/train reject <A> <B>`
Rejects a pending link request.

**Requirements:**
- Must own station B (the receiving station)
- A pending request must exist

**Usage:** `/train reject <initiatingStation> <yourStation>`

**Example:**
```
/train reject PlayerStation MyStation
```

---

### `/train transfer <station> <player>`
Transfers station ownership to another player.

**Requirements:**
- Must be station owner OR admin
- Target player must be online

**Usage:** `/train transfer <stationName> <playerName>`

**Example:**
```
/train transfer MyStation Steve
```

---

### `/train message <station> <message>`
Sets the welcome message shown when players arrive at a station.

**Requirements:**
- Must be station owner OR admin

**Features:**
- Supports color codes with `&`
- Message is shown on arrival

**Usage:** `/train message <stationName> <message>`

**Examples:**
```
/train message CentralStation Welcome to the center of the city!
/train message Market &6Welcome to the &eMarket&6!
/train message VIP &c&lVIP Area - Members Only
```

---

### `/train block <material>`
Changes the block required under rails to create stations.

**Requirements:**
- Admin permission only

**Usage:** `/train block <materialName>`

**Examples:**
```
/train block DIAMOND_BLOCK
/train block EMERALD_BLOCK
/train block IRON_BLOCK
```

> Use valid Bukkit material names (e.g., `GOLD_BLOCK`, `DIAMOND_BLOCK`)

---

### `/train reload`
Reloads all configuration files without restarting the server.

**Requirements:**
- Admin permission only

**What gets reloaded:**
- `config.yml` - Main settings
- `messages.yml` - All messages
- Sound settings
- Creation cost settings
- Travel cost settings

**Usage:** `/train reload`

> **Note:** Station data is not reloaded (it's always in sync).

---

## Command Aliases

- `/train` - Main command
- `/sttrain` - Alias for `/train`

---

## See Also

- [Permissions](Permissions) - Required permissions for commands
- [Configuration](Configuration) - Adjust XP costs and settings
- [GUI Guide](GUI-Guide) - Using the GUI instead of commands
