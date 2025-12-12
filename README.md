# SimpleTrains

A lightweight Minecraft plugin for creating fast-travel rail networks with train stations.

**Version:** 1.3.0 | **Minecraft:** 1.20+ | **Java:** 17+ | **Author:** AndresTube

[![SpigotMC](https://img.shields.io/badge/SpigotMC-SimpleTrains-orange)](https://www.spigotmc.org/)
[![Modrinth](https://img.shields.io/badge/Modrinth-SimpleTrains-green)](https://modrinth.com/)

---

## Features

### Core System
- Create train stations on rails with a configurable base block (default: Gold Block)
- Teleport between linked stations using registered minecarts
- Station ownership with transfer capability
- Bidirectional station linking (Public/Private modes)
- Full GUI-based management interface

### Station Management
- Create, delete, and configure stations via commands or GUI
- Custom welcome messages with color code support (`&`)
- Find nearby stations within range
- Link request system with owner approval

### Minecart Registration
- Use a renamed Name Tag on a minecart to make it "warp-eligible"
- Only registered minecarts can teleport between stations

### Link System
- Request-based linking with owner approval
- **Public links:** Anyone can travel
- **Private links:** Only station owners can use

### Sound Effects (v1.3.0)
- Configurable departure and arrival sounds
- Adjustable volume and pitch per sound
- Can be completely disabled

### Creation Costs (v1.3.0)
- Optional cost to create stations
- Supports **XP levels**, **Vault economy**, or **items**
- Custom item requirements with name matching
- Admins bypass all costs

### Travel Costs (v1.3.0)
- Optional cost per travel
- Supports **XP levels**, **Vault economy**, or **items**
- Custom item requirements (e.g., "Train Ticket")
- Admins bypass all costs

### Fully Customizable Messages
- All plugin messages are customizable via `messages.yml`
- Support for color codes and placeholders
- GUI titles, item names, and lore are all editable
- Full multi-language support

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/train set <name>` | Create a station | Owner |
| `/train delete <name>` | Delete a station | Owner/Admin |
| `/train link <A> <B> [type]` | Link stations (public/private) | Owner of A |
| `/train unlink <A> <B>` | Remove a link | Owner of either |
| `/train transfer <station> <player>` | Transfer ownership | Owner/Admin |
| `/train near` | List nearby stations | All |
| `/train gui` | Open station management GUI | All |
| `/train message <station> <msg>` | Set welcome message | Owner/Admin |
| `/train accept <A> <B>` | Accept link request | Owner of B |
| `/train reject <A> <B>` | Reject link request | Owner of B |
| `/train block <material>` | Set creation block | Admin |
| `/train reload` | Reload configuration | Admin |
| `/train help` | Show help menu | All |

---

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `simpletrains.use` | Basic commands | `true` |
| `simpletrains.admin` | Admin override for all actions | `op` |
| `simpletrains.create` | Create new stations | `op` |

---

## Configuration

### config.yml

```yaml
settings:
  creation_block: GOLD_BLOCK      # Block required under rail to create station

# Creation cost (v1.3.0) - Cost to create new stations
creation_cost:
  enabled: true
  type: XP                        # XP, VAULT, or ITEM
  amount: 5
  item:
    material: GOLD_INGOT
    amount: 1
    name: ""                      # Optional: require specific item name

# Sound configuration (v1.3.0)
sounds:
  enabled: true
  departure:
    sound: ENTITY_ENDERMAN_TELEPORT
    volume: 1.0
    pitch: 1.0
  arrival:
    sound: BLOCK_NOTE_BLOCK_CHIME
    volume: 1.0
    pitch: 1.2

# Travel cost configuration (v1.3.0)
travel_cost:
  enabled: false                  # Enable to charge players per travel
  type: XP                        # XP, VAULT, or ITEM
  amount: 1
  item:
    material: GOLD_INGOT
    amount: 1
    name: ""                      # Optional: require specific item name

messages:
  warp_confirm: '&aWarping to %DESTINATION%...'
  station_welcome: '&e>> &6%MESSAGE%'
```

### messages.yml

All plugin messages are customizable! Edit `messages.yml` to change any text:

```yaml
prefix: "&6[SimpleTrains] &r"
station-created: "&aStation '%STATION%' created successfully! (-%COST% XP)"
no-permission: "&cYou don't have permission for this command."
# ... 100+ customizable messages
```

**Placeholders:** Use `%PLACEHOLDER%` format (e.g., `%STATION%`, `%PLAYER%`, `%COST%`)

---

## Quick Start

1. **Create a station:**
   - Place a rail on top of a Gold Block
   - Stand on the rail
   - Run `/train set MyStation`

2. **Register a minecart:**
   - Rename a Name Tag in an anvil (any name)
   - Right-click a minecart with it

3. **Link stations:**
   - Run `/train link StationA StationB`
   - The owner of StationB must accept the request

4. **Travel:**
   - Ride your registered minecart to a station
   - Select your destination from the GUI

---

## Technical Info

| Setting | Value |
|---------|-------|
| Teleport cooldown | 10 seconds |
| Player detection radius | 20 blocks |
| Near command range | 500 blocks |
| Data storage | YAML (auto-save) |
| Minecraft versions | 1.20.x, 1.21.x+ |
| Java version | 17+ |

---

## Changelog

### v1.3.0
- Added **sound effects** for departure and arrival
  - Configurable sounds, volume, and pitch
  - Can be disabled entirely
- Added **creation cost system**
  - Supports XP levels, Vault economy, or items
  - Custom item requirements with optional name matching
  - Admins bypass costs automatically
- Added **travel cost system**
  - Supports XP levels, Vault economy, or items
  - Custom item requirements with optional name matching
  - Admins bypass costs automatically
- Added `/train reload` command for hot-reloading configuration
- Added full **tab completion** for all commands
- Fixed GUI compatibility with translated messages
- Optional Vault integration (soft-dependency)

### v1.2.0
- Added complete messages system (`messages.yml`)
- All plugin text is now fully customizable
- GUI titles, item names, and lore can be edited
- Support for placeholders in all messages

### v1.1.0
- Added support for Minecraft 1.20.x+
- Lowered Java requirement from 21 to 17
- Improved backwards compatibility

### v1.0.0
- Initial release
- Station creation and management
- Minecart registration system
- Link request system with approval
- GUI interface

---

## Dependencies

| Dependency | Required | Purpose |
|------------|----------|---------|
| Paper/Spigot 1.20+ | Yes | Server API |
| Java 17+ | Yes | Runtime |
| Vault | No | Economy support |

---

## Support

For issues or feature requests, please open an issue on the [GitHub repository](https://github.com/AndresTube/SimpleTrains).

---

## License

This project is open source. Feel free to use, modify, and distribute.
