# Getting Started

This guide will walk you through setting up your first train station network.

---

## Step 1: Install the Plugin

1. Download `SimpleTrains-1.2.0.jar` from the releases page
2. Place the file in your server's `plugins` folder
3. Restart your server (or use a plugin manager to load it)
4. Verify it loaded by checking console for: `SimpleTrains has been enabled!`

---

## Step 2: Create Your First Station

### Requirements
- A **rail** (any type: normal, powered, detector, activator)
- A **Gold Block** (or your configured creation block) placed directly under the rail
- **5 XP levels** (default cost, configurable)

### Steps

1. Place a Gold Block on the ground
2. Place a rail on top of the Gold Block
3. Stand on the rail
4. Run the command: `/train set MyFirstStation`

```
   [RAIL]      <-- Stand here
[GOLD_BLOCK]   <-- Base block
```

If successful, you'll see:
```
[SimpleTrains] Station 'MyFirstStation' created successfully! (-5 XP)
```

---

## Step 3: Register a Minecart

Only **registered minecarts** can teleport between stations.

### How to Register

1. Get a **Name Tag**
2. Use an **Anvil** to rename it (any name works, e.g., "Train")
3. Right-click a **Minecart** with the renamed Name Tag
4. The minecart is now warp-eligible!

> **Tip:** The Name Tag is consumed when you register the minecart.

---

## Step 4: Create a Second Station

Repeat Step 2 to create another station:

1. Go to a different location
2. Place Gold Block + Rail
3. Stand on the rail
4. Run: `/train set MySecondStation`

---

## Step 5: Link the Stations

Stations must be linked before you can travel between them.

### Link Command
```
/train link MyFirstStation MySecondStation
```

### What Happens?

**If you own both stations:**
- They're linked instantly as PUBLIC

**If someone else owns the second station:**
- A link request is sent to the owner
- They must accept with: `/train accept MyFirstStation MySecondStation`

---

## Step 6: Travel!

1. Place your registered minecart on a station rail
2. Ride the minecart (right-click to enter)
3. When it reaches the station block, it will stop
4. A GUI will open showing available destinations
5. Click a destination to teleport!

---

## Quick Reference

| Action | Command |
|--------|---------|
| Create station | `/train set <name>` |
| Delete station | `/train delete <name>` |
| Link stations | `/train link <A> <B>` |
| Open GUI | `/train gui` |
| Get help | `/train help` |

---

## Next Steps

- Learn all [Commands](Commands)
- Set up [Permissions](Permissions) for your players
- Customize [Configuration](Configuration)
- Personalize [Messages](Messages-Customization)

---

## Troubleshooting

### "Station already exists"
A station with that name already exists. Choose a different name.

### "You need X XP levels"
You don't have enough experience. Gain more XP or ask an admin to lower the cost.

### "You must be standing on a RAIL block"
Make sure you're standing directly on a rail, not next to it.

### Minecart doesn't teleport
Make sure the minecart is registered with a Name Tag.
