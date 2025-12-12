# Permissions

SimpleTrains uses a simple permission system to control access to features.

---

## Permission Nodes

| Permission | Description | Default |
|------------|-------------|---------|
| `simpletrains.use` | Use basic commands and travel | `true` |
| `simpletrains.create` | Create new stations | `op` |
| `simpletrains.admin` | Full admin access | `op` |

---

## Detailed Permissions

### `simpletrains.use`

**Default:** `true` (all players)

Allows players to:
- Use `/train gui` to open the station menu
- Use `/train near` to find nearby stations
- Use `/train help` to view help
- Travel between stations using minecarts
- View station information

**Commands enabled:**
- `/train gui`
- `/train near`
- `/train help`

---

### `simpletrains.create`

**Default:** `op` (operators only)

Allows players to:
- Create new stations with `/train set`
- Delete their own stations
- Link their stations to others
- Unlink their stations
- Transfer ownership of their stations
- Set welcome messages on their stations
- Accept/reject link requests

**Commands enabled:**
- `/train set <name>`
- `/train delete <name>` (own stations)
- `/train link <A> <B>` (own stations)
- `/train unlink <A> <B>` (own stations)
- `/train transfer <station> <player>` (own stations)
- `/train message <station> <msg>` (own stations)
- `/train accept <A> <B>`
- `/train reject <A> <B>`

---

### `simpletrains.admin`

**Default:** `op` (operators only)

Full administrative access:
- All permissions from `simpletrains.use`
- All permissions from `simpletrains.create`
- Bypass ownership checks
- Delete any station
- Modify any station's settings
- Change the creation block type
- No XP cost for actions

**Additional commands:**
- `/train block <material>`
- All commands work on any station (bypass ownership)

---

## Permission Setup Examples

### LuckPerms

```yaml
# Allow all players to create stations
/lp group default permission set simpletrains.create true

# Give admin permissions to staff
/lp group admin permission set simpletrains.admin true
```

### PermissionsEx

```yaml
groups:
  default:
    permissions:
    - simpletrains.use
    - simpletrains.create
  admin:
    permissions:
    - simpletrains.admin
```

### GroupManager

```yaml
groups:
  Default:
    permissions:
    - simpletrains.use
    - simpletrains.create
  Admin:
    permissions:
    - simpletrains.admin
```

### Vanilla (ops.json)

By default, operators have all permissions. No configuration needed.

---

## Common Setups

### Public Server (Everyone can create)

Give all players creation permission:
```
simpletrains.use: true (default)
simpletrains.create: true (set for default group)
```

### Restricted Server (Only staff can create)

Keep default settings:
```
simpletrains.use: true (default)
simpletrains.create: op (default)
```

### RPG Server (Earn station creation)

Create a special rank for station creators:
```yaml
# LuckPerms example
/lp group traveler permission set simpletrains.create true
```

---

## Permission Inheritance

```
simpletrains.admin
    └── simpletrains.create
            └── simpletrains.use
```

- `simpletrains.admin` includes all permissions
- `simpletrains.create` includes `simpletrains.use`

---

## Troubleshooting

### "You don't have permission"
The player lacks the required permission. Check:
1. Permission plugin is working
2. Player has correct group
3. Permission node is spelled correctly

### Players can't create stations
Give them `simpletrains.create`:
```
/lp user <player> permission set simpletrains.create true
```

### Admin can't bypass ownership
Make sure they have `simpletrains.admin`:
```
/lp user <player> permission set simpletrains.admin true
```

---

## See Also

- [Commands](Commands) - What each command does
- [Configuration](Configuration) - Server settings
