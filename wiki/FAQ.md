# Frequently Asked Questions

Common questions and answers about SimpleTrains.

---

## General Questions

### What is SimpleTrains?

SimpleTrains is a Minecraft plugin that lets you create train stations and teleport between them using minecarts. It's perfect for creating public transportation systems on your server.

### What server versions are supported?

- **Minecraft:** 1.20.x and higher (1.20, 1.21, etc.)
- **Server Software:** Paper, Spigot, and their forks
- **Java:** 17 or higher

### Is SimpleTrains free?

Yes! SimpleTrains is completely free and open source.

---

## Station Questions

### How do I create a station?

1. Place a Gold Block (or configured block)
2. Place any rail on top
3. Stand on the rail
4. Run `/train set <name>`

See [Getting Started](Getting-Started) for a detailed guide.

### Why can't I create a station?

Common reasons:
- **Not on a rail** - You must be standing on a rail block
- **Wrong base block** - Check what block is required (default: Gold Block)
- **Not enough XP** - You need enough experience levels
- **No permission** - You need `simpletrains.create`
- **Name taken** - Choose a different station name

### Can I move a station?

No, stations cannot be moved. You must delete it and create a new one at the desired location.

### How do I delete a station?

```
/train delete <stationName>
```
You must be the owner or have admin permission.

### What happens to links when I delete a station?

All links to and from that station are automatically removed.

---

## Minecart Questions

### How do I register a minecart?

1. Get a **Name Tag**
2. Rename it in an **Anvil** (any name works)
3. **Right-click** a minecart with the Name Tag
4. The minecart is now registered!

### Why isn't my minecart teleporting?

- **Not registered** - Use a renamed Name Tag on it
- **Not on a station** - Make sure you're at a station location
- **No links** - The station needs to be linked to destinations
- **Private link** - You may not have access to private links

### Do I need to be riding the minecart?

No! You can push the minecart or stand near it. The plugin detects players within 20 blocks.

### Can I use special minecarts?

Yes, any minecart type works:
- Regular Minecart
- Minecart with Chest
- Minecart with Hopper
- Minecart with Furnace
- Minecart with TNT
- Minecart with Command Block

---

## Linking Questions

### How do I link two stations?

```
/train link <stationA> <stationB>
```
You must own station A. If you don't own station B, a request is sent.

### What's the difference between PUBLIC and PRIVATE links?

- **PUBLIC** - Anyone can use this link to travel
- **PRIVATE** - Only the station owners can use this link

### How do I accept a link request?

```
/train accept <theirStation> <yourStation>
```
Or use the GUI: Station Config → Link Manager → Pending Requests

### Why didn't my link work?

- **Already linked** - Stations are already connected
- **Station doesn't exist** - Check the station name spelling
- **Not the owner** - You must own the initiating station
- **Waiting for approval** - The other owner needs to accept

### Are links bidirectional?

Yes! When you link A to B, travel works both ways.

---

## Creation Cost Questions (v1.3.0)

### How do I charge players to create stations?

Enable creation costs in `config.yml`:
```yaml
creation_cost:
  enabled: true
  type: XP      # XP, VAULT, or ITEM
  amount: 5
```

### What cost types are available for creation?

- **XP** - Experience levels
- **VAULT** - Money (requires Vault plugin + economy plugin)
- **ITEM** - Physical items from inventory

### How do I disable creation costs?

```yaml
creation_cost:
  enabled: false
```

### Can I require a specific item to create stations?

Yes! Use item-based costs:
```yaml
creation_cost:
  enabled: true
  type: ITEM
  item:
    material: PAPER
    amount: 1
    name: "Station Permit"
```

### Do admins pay creation costs?

No, players with `simpletrains.admin` bypass all costs.

---

## Sound Questions (v1.3.0)

### How do I change the sounds?

Edit `config.yml`:
```yaml
sounds:
  departure:
    sound: ENTITY_ENDERMAN_TELEPORT
    volume: 1.0
    pitch: 1.0
  arrival:
    sound: BLOCK_NOTE_BLOCK_CHIME
    volume: 1.0
    pitch: 1.2
```

### Where can I find sound names?

Use the [Bukkit Sound documentation](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html).

### How do I disable sounds?

```yaml
sounds:
  enabled: false
```

---

## Travel Cost Questions (v1.3.0)

### How do I charge players to travel?

Enable travel costs in `config.yml`:
```yaml
travel_cost:
  enabled: true
  type: XP      # XP, VAULT, or ITEM
  amount: 1
```

### What cost types are available?

- **XP** - Experience levels
- **VAULT** - Money (requires Vault plugin + economy plugin)
- **ITEM** - Physical items from inventory

### Do I need Vault installed?

Only if you want to use `type: VAULT`. For XP or ITEM costs, Vault is not required.

### How do I use item-based costs?

```yaml
travel_cost:
  enabled: true
  type: ITEM
  item:
    material: GOLD_INGOT
    amount: 1
    name: ""   # Leave empty for any gold ingot
```

### Can I require a specific named item?

Yes! Set the `name` field:
```yaml
travel_cost:
  type: ITEM
  item:
    material: PAPER
    amount: 1
    name: "Train Ticket"
```

### Do admins pay travel costs?

No, players with `simpletrains.admin` bypass travel costs.

---

## Permission Questions

### What permissions do players need?

- **Basic use:** `simpletrains.use` (default: true)
- **Create stations:** `simpletrains.create` (default: op)
- **Admin access:** `simpletrains.admin` (default: op)

### How do I let everyone create stations?

Give all players the `simpletrains.create` permission:
```
/lp group default permission set simpletrains.create true
```

### Why can't a player configure their station?

They need `simpletrains.create` permission to manage their stations.

---

## Configuration Questions

### Where are the config files?

```
plugins/SimpleTrains/
├── config.yml      # Main settings
├── messages.yml    # All messages
└── data.yml        # Station data
```

### How do I change the creation block?

**In-game:** `/train block DIAMOND_BLOCK`

**In config.yml:**
```yaml
settings:
  creation_block: DIAMOND_BLOCK
```

### How do I customize messages?

Edit `messages.yml`. See [Messages Customization](Messages-Customization).

### How do I reset to defaults?

1. Stop the server
2. Delete the file you want to reset
3. Start the server (new file generated)

---

## Troubleshooting

### Plugin won't load

Check:
1. Java 17+ is installed
2. Server is Paper/Spigot 1.20+
3. No errors in console
4. File is named correctly (`.jar`)

### Stations not saving

- Check file permissions in plugins folder
- Look for errors in console
- Don't edit `data.yml` manually

### GUI not opening

- Make sure you're a player (not console)
- Check for permission issues
- Look for errors in console

### Commands not working

- Check spelling
- Verify permissions
- Make sure plugin is loaded (`/plugins`)

### Minecart stuck at station

- Cooldown is 10 seconds between teleports
- Check if destinations are available
- Verify the minecart is registered

---

## Other Questions

### Can I use this with other plugins?

SimpleTrains is standalone and should work with most plugins. No known conflicts.

### Does it work with Multiverse?

Yes! Stations can be in different worlds, and travel works across worlds.

### Is there an API?

Currently no public API. Contact the developer if you need one.

### How do I report bugs?

Open an issue on the GitHub repository or contact AndresTube.

### Can I contribute?

Yes! The plugin is open source. Pull requests are welcome.

---

## Quick Reference

| Problem | Solution |
|---------|----------|
| Can't create station | Check rail + base block + XP + permissions |
| Minecart won't teleport | Register it with a Name Tag |
| Link not working | Check ownership and wait for approval |
| GUI won't open | Check permissions, must be a player |
| Messages wrong | Edit `messages.yml` |
| Costs too high | Edit `config.yml` |

---

## See Also

- [Getting Started](Getting-Started)
- [Commands](Commands)
- [Permissions](Permissions)
- [Configuration](Configuration)
