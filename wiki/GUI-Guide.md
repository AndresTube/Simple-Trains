# GUI Guide

SimpleTrains features a complete GUI system for managing stations without using commands.

---

## Opening the GUI

**Command:** `/train gui`

This opens the main Station List GUI.

---

## Station List GUI

The main menu showing all stations.

### Layout

```
┌─────────────────────────────────────────────────────┐
│ [Station] [Station] [Station] [Station] [Station]   │
│ [Station] [Station] [Station] ...                   │
│                                                     │
│                                                     │
│                                                     │
├─────────────────────────────────────────────────────┤
│ [◄ Prev] [  ] [Toggle] [  ] [Page] [  ] [  ] [Next ►]│
└─────────────────────────────────────────────────────┘
```

### Elements

| Item | Description |
|------|-------------|
| **Compass** (Station) | A station - click to configure |
| **Arrow** (◄ Prev) | Go to previous page |
| **Arrow** (Next ►) | Go to next page |
| **Ender Pearl / Nether Star** | Toggle between "My Stations" and "All Stations" |
| **Paper** | Current page info |

### Station Item Info

Each station compass shows:
- **Name** - Station name (title)
- **Owner** - Who owns this station
- **Linked Stations** - Number of connections
- **Location** - X, Z coordinates

### View Modes

- **My Stations** - Only shows stations you own
- **All Stations** - Shows every station on the server

Click the toggle button (Ender Pearl/Nether Star) to switch.

---

## Station Configuration GUI

Click on a station you own to open its configuration.

### Layout

```
┌─────────────────────────────────────────────┐
│                                             │
│    [Edit Msg]    [Links]    [DELETE]        │
│                                             │
│                  [Back]                     │
└─────────────────────────────────────────────┘
```

### Options

| Item | Description |
|------|-------------|
| **Book** (Edit Welcome Message) | Set the message shown on arrival |
| **Chain** (Link Manager) | Manage station connections |
| **Barrier** (DELETE STATION) | Delete this station |
| **Glass Pane** (Back) | Return to Station List |

---

## Edit Welcome Message

1. Click the **Book** icon
2. GUI closes
3. Type your message in chat
4. Supports color codes (`&a`, `&b`, etc.)
5. Type `cancel` to cancel

**Example:**
```
Welcome to the market! Shop here!
```

Or with colors:
```
&6Welcome to &ethe market&6! &aShop here!
```

---

## Link Manager GUI

Manage all connections for a station.

### Layout

```
┌─────────────────────────────────────────────────────┐
│ [Header decoration]                                 │
├─────────────────────────────────────────────────────┤
│ [Link] [Link] [Link] ...                            │
│                                                     │
│                                                     │
├─────────────────────────────────────────────────────┤
│ [  ] [Request] [  ] [Back] [  ] [Pending] [  ] [  ] │
└─────────────────────────────────────────────────────┘
```

### Elements

| Item | Description |
|------|-------------|
| **Chain** (Link) | A PUBLIC link - click to unlink |
| **Lever** (Link) | A PRIVATE link - click to unlink |
| **Paper** (Request New Link) | Request a connection to another station |
| **Clock** (View Pending) | See incoming link requests |
| **Arrow** (Back) | Return to Station Configuration |

### Existing Links

Each linked station shows:
- Station name
- Link type (PUBLIC or PRIVATE)
- "Left Click: UNLINK STATION"

**Click to unlink** - removes the connection immediately.

---

## Request New Link

1. Click **Request New Link** (Paper icon)
2. GUI closes
3. Type the name of the station you want to link to
4. If you own both: linked instantly
5. If someone else owns it: request sent to them
6. Type `cancel` to cancel

---

## Pending Requests GUI

View and respond to incoming link requests.

### Layout

```
┌─────────────────────────────────────────────────────┐
│ [Header decoration]                                 │
├─────────────────────────────────────────────────────┤
│ [Request] [Request] ...                             │
│                                                     │
│                                                     │
├─────────────────────────────────────────────────────┤
│ [  ] [  ] [  ] [Back] [  ] [  ] [  ] [  ] [  ]     │
└─────────────────────────────────────────────────────┘
```

### Request Item Info

Each pending request shows:
- Which station wants to link
- Who sent the request
- **Left Click**: Accept (costs XP)
- **Right Click**: Reject

### Responding to Requests

- **Left Click** - Accept the link (costs XP, creates connection)
- **Right Click** - Reject the link (no cost, removes request)

---

## Destination Selection GUI

When riding a minecart to a station with multiple destinations.

### Layout

```
┌─────────────────────────────────────┐
│ [Dest] [Dest] [Dest] [Dest] [Dest]  │
│ [Dest] [Dest] ...                   │
└─────────────────────────────────────┘
```

### Elements

Each destination shows:
- Station name
- "From: [current station]"
- Link type (PUBLIC/PRIVATE)

**Click a destination** to teleport there!

---

## GUI Navigation Map

```
/train gui
    │
    ▼
Station List
    │
    ├── Click Station ──► Station Configuration
    │                           │
    │                           ├── Edit Message ──► Chat Input
    │                           │
    │                           ├── Link Manager
    │                           │       │
    │                           │       ├── Request Link ──► Chat Input
    │                           │       │
    │                           │       └── Pending Requests
    │                           │               │
    │                           │               └── Accept/Reject
    │                           │
    │                           └── Delete ──► Confirmation in Chat
    │
    └── Toggle View (My/All Stations)
```

---

## Tips

1. **Shift-click** doesn't work in these GUIs (disabled for safety)
2. Use **/train gui** as a shortcut instead of navigating
3. Station owners see a configuration option; others just see info
4. The GUI refreshes automatically after actions

---

## See Also

- [Commands](Commands) - Command alternatives
- [Getting Started](Getting-Started) - Basic setup
- [Messages Customization](Messages-Customization) - Change GUI text
