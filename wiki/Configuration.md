# Configuration

SimpleTrains uses two configuration files:
- `config.yml` - Main plugin settings
- `messages.yml` - All customizable messages (see [Messages Customization](Messages-Customization))

Both files are located in `plugins/SimpleTrains/`.

---

## config.yml

```yaml
settings:
  # The block required under rails to create a station
  creation_block: GOLD_BLOCK

# Creation cost configuration (v1.3.0)
creation_cost:
  enabled: true
  type: XP          # XP, VAULT, or ITEM
  amount: 5
  item:
    material: GOLD_INGOT
    amount: 1
    name: ""        # Optional custom name requirement

# Sound effects configuration (v1.3.0)
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
  enabled: false
  type: XP          # XP, VAULT, or ITEM
  amount: 1
  item:
    material: GOLD_INGOT
    amount: 1
    name: ""        # Optional custom name requirement

messages:
  # Message shown when teleporting
  warp_confirm: '&aWarping to %DESTINATION%...'

  # Welcome message format
  station_welcome: '&e>> &6%MESSAGE%'
```

---

## Settings Explained

### `creation_block`

**Default:** `GOLD_BLOCK`

The block that must be placed under a rail to create a station.

**Valid values:** Any valid Bukkit material name

**Examples:**
```yaml
creation_block: GOLD_BLOCK
creation_block: DIAMOND_BLOCK
creation_block: EMERALD_BLOCK
creation_block: IRON_BLOCK
creation_block: LAPIS_BLOCK
```

**Tip:** Use `/train block <material>` to change this in-game.

---

## Creation Cost Configuration (v1.3.0)

### `creation_cost.enabled`

**Default:** `true`

Enable or disable station creation costs.

```yaml
creation_cost:
  enabled: true    # Charge players to create stations
  enabled: false   # Free station creation
```

---

### `creation_cost.type`

**Default:** `XP`

The type of currency to charge for station creation.

**Options:**
- `XP` - Experience levels
- `VAULT` - Economy money (requires Vault plugin)
- `ITEM` - Physical items from inventory

```yaml
creation_cost:
  type: XP      # Charge XP levels
  type: VAULT   # Charge money (needs Vault)
  type: ITEM    # Charge items
```

---

### `creation_cost.amount`

**Default:** `5`

The amount to charge (for XP and VAULT types).

```yaml
creation_cost:
  amount: 0     # Free
  amount: 5     # Default
  amount: 50    # Expensive
```

**Note:** Players with `simpletrains.admin` bypass this cost.

---

### `creation_cost.item`

Configure item-based creation costs.

**Options:**
- `material` - Bukkit material name
- `amount` - Number of items required
- `name` - (Optional) Custom display name requirement

```yaml
# Charge 1 gold ingot
creation_cost:
  type: ITEM
  item:
    material: GOLD_INGOT
    amount: 1
    name: ""

# Charge 5 diamonds
creation_cost:
  type: ITEM
  item:
    material: DIAMOND
    amount: 5
    name: ""

# Charge a custom "Station Permit" item
creation_cost:
  type: ITEM
  item:
    material: PAPER
    amount: 1
    name: "Station Permit"
```

---

## Creation Cost Examples

### Free Server
```yaml
creation_cost:
  enabled: false
```

### XP-Based (Default)
```yaml
creation_cost:
  enabled: true
  type: XP
  amount: 5
```

### Vault Economy
```yaml
creation_cost:
  enabled: true
  type: VAULT
  amount: 1000
```

### Item-Based (Station Permits)
```yaml
creation_cost:
  enabled: true
  type: ITEM
  item:
    material: PAPER
    amount: 1
    name: "Station Permit"
```

---

## Creation Block Examples

### Default (Gold)
```yaml
settings:
  creation_block: GOLD_BLOCK
```
Easy to obtain, good for survival servers.

### Premium (Diamond)
```yaml
settings:
  creation_block: DIAMOND_BLOCK
```
More expensive, limits station spam.

### Rare (Emerald)
```yaml
settings:
  creation_block: EMERALD_BLOCK
```
Encourages trading with villagers.

### Easy (Iron)
```yaml
settings:
  creation_block: IRON_BLOCK
```
Very accessible for new players.

### Creative (Any block)
```yaml
settings:
  creation_block: STONE
```
For creative/build servers.

---

## Applying Changes

After editing `config.yml`:

1. **Save the file**
2. **Restart the server** or use a plugin manager to reload

> **Note:** Some changes (like `creation_block`) can be changed in-game with `/train block <material>`.

---

## File Locations

```
plugins/
└── SimpleTrains/
    ├── config.yml        # Main settings
    ├── messages.yml      # All messages
    └── data.yml          # Station data (don't edit manually)
```

---

## Sounds Configuration (v1.3.0)

### `sounds.enabled`

**Default:** `true`

Enable or disable all sound effects.

```yaml
sounds:
  enabled: true    # Sounds on
  enabled: false   # Sounds off
```

---

### `sounds.departure` / `sounds.arrival`

Configure the sounds played when departing/arriving.

**Options:**
- `sound` - Bukkit Sound enum name ([full list](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html))
- `volume` - Volume level (0.0 to 1.0)
- `pitch` - Pitch modifier (0.5 to 2.0)

**Example Sounds:**
```yaml
sounds:
  departure:
    sound: ENTITY_ENDERMAN_TELEPORT   # Default
    sound: BLOCK_PORTAL_TRIGGER       # Portal whoosh
    sound: ENTITY_ILLUSIONER_MIRROR_MOVE  # Magical
    sound: ENTITY_SHULKER_TELEPORT    # Shulker teleport

  arrival:
    sound: BLOCK_NOTE_BLOCK_CHIME     # Default
    sound: BLOCK_BEACON_ACTIVATE      # Beacon sound
    sound: ENTITY_PLAYER_LEVELUP      # Level up sound
    sound: BLOCK_AMETHYST_BLOCK_CHIME # Amethyst chime
```

---

## Travel Cost Configuration (v1.3.0)

### `travel_cost.enabled`

**Default:** `false`

Enable or disable travel costs.

```yaml
travel_cost:
  enabled: false   # Free travel (default)
  enabled: true    # Charge players to travel
```

---

### `travel_cost.type`

**Default:** `XP`

The type of currency to charge.

**Options:**
- `XP` - Experience levels
- `VAULT` - Economy money (requires Vault plugin)
- `ITEM` - Physical items from inventory

```yaml
travel_cost:
  type: XP      # Charge XP levels
  type: VAULT   # Charge money (needs Vault)
  type: ITEM    # Charge items
```

---

### `travel_cost.amount`

**Default:** `1`

The amount to charge (for XP and VAULT types).

```yaml
travel_cost:
  amount: 1     # 1 XP level or $1
  amount: 5     # 5 XP levels or $5
  amount: 100   # 100 XP levels or $100
```

---

### `travel_cost.item`

Configure item-based travel costs.

**Options:**
- `material` - Bukkit material name
- `amount` - Number of items required
- `name` - (Optional) Custom display name requirement

```yaml
# Charge 1 gold ingot
travel_cost:
  type: ITEM
  item:
    material: GOLD_INGOT
    amount: 1
    name: ""

# Charge 5 emeralds
travel_cost:
  type: ITEM
  item:
    material: EMERALD
    amount: 5
    name: ""

# Charge a custom "Train Ticket" item
travel_cost:
  type: ITEM
  item:
    material: PAPER
    amount: 1
    name: "Train Ticket"
```

---

## Travel Cost Examples

### Free Server (Default)
```yaml
travel_cost:
  enabled: false
```

### XP-Based Economy
```yaml
travel_cost:
  enabled: true
  type: XP
  amount: 2
```

### Vault Economy
```yaml
travel_cost:
  enabled: true
  type: VAULT
  amount: 50
```

### Item-Based (Train Tickets)
```yaml
travel_cost:
  enabled: true
  type: ITEM
  item:
    material: PAPER
    amount: 1
    name: "Train Ticket"
```

### Expensive RPG Server
```yaml
travel_cost:
  enabled: true
  type: XP
  amount: 5
```

---

## See Also

- [Messages Customization](Messages-Customization) - Customize all text
- [Permissions](Permissions) - Control who can do what
- [Commands](Commands) - In-game configuration commands
