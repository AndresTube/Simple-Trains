# SimpleTrains

A lightweight Minecraft plugin for creating fast-travel rail networks with train stations.

**Version:** 1.2.0 | **API:** 1.20+ | **Author:** AndresTube

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
- XP cost system for creation and linking

### Minecart Registration
- Use a renamed Name Tag on a minecart to make it "warp-eligible"
- Only registered minecarts can teleport between stations

### Link System
- Request-based linking with owner approval
- **Public links:** Anyone can travel
- **Private links:** Only station owners can use

### Fully Customizable Messages (NEW in 1.2.0)
- All plugin messages are customizable via `messages.yml`
- Support for color codes and placeholders
- GUI titles, item names, and lore are all editable

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
  creation_xp_cost: 5             # XP levels to create a station
  link_creation_xp_cost: 3        # XP levels to request a link
  link_acceptance_xp_cost: 2      # XP levels to accept a link
  linking_requires_owner_acceptance: true  # Require approval for links

messages:
  warp_confirm: '&aWarping to %DESTINATION%...'
  station_welcome: '&e>> Welcome to %STATION% station! &6%MESSAGE%'
```

### messages.yml (NEW in 1.2.0)

All plugin messages are now customizable! Edit `messages.yml` to change any text:

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

## Support

For issues or suggestions, contact **AndresTube** or open an issue on the repository.
