  # SimpleTrains - Minecraft Plugin

  Actual Version: 1.0.0 | API: 1.21 | Author: AndresTube

  Description

  A fast-travel rail system plugin that lets players create train stations
  and warp between them using minecarts.

  ---
  Features

  Core System
  - Create stations on rails with a base block (default: Gold Block)
  - Teleport between linked stations using registered minecarts
  - Station ownership with transfer capability
  - Bidirectional station linking (Public/Private)
  - Full GUI-based management interface

  Station Management
  - Create, delete, and configure stations
  - Custom welcome messages with color code support (&)
  - Find nearby stations (within 500 blocks)
  - XP cost system for creation and linking

  Minecart Registration
  - Use a renamed Name Tag on a minecart to make it "warp-eligible"
  - Only registered minecarts can teleport between stations

  Link System
  - Request-based linking (owner approval required)
  - Public links: anyone can travel
  - Private links: only owners can use

  ---
  Commands

  - /train set  - Create a station (requires simpletrains.create)
  - /train delete  - Delete a station (Owner/Admin)
  - /train link   [type] - Link stations [public/private] (Owner of A)
  - /train unlink   - Remove link (Owner of either)
  - /train transfer   - Transfer ownership (Owner/Admin)
  - /train near - List nearby stations
  - /train gui - Open station GUI
  - /train message   - Set welcome message (Owner/Admin)
  - /train accept   - Accept link request (Owner of B)
  - /train reject   - Reject link request (Owner of B)
  - /train block  - Set creation block (Admin only)
  - /train help - Show help

  Alias: /sttrain

  ---
  Permissions

  - simpletrains.use - Basic commands (default: true)
  - simpletrains.admin - Admin override (default: op)
  - simpletrains.create - Create stations (default: op)

  ---
  Configuration

  settings.creation_block: GOLD_BLOCK

  settings.creation_xp_cost: 5

  settings.link_creation_xp_cost: 3

  settings.link_acceptance_xp_cost: 2

  settings.linking_requires_owner_acceptance: true

  messages.warp_confirm: &aWarping to %DESTINATION%...

  messages.station_welcome: &e>> Welcome to %STATION% station! &6%MESSAGE%

  ---
  Quick Start

  1. Create a station: Place a rail on a Gold Block, stand on it, run /train
   set MyStation
  2. Register a minecart: Rename a Name Tag in an anvil, right-click a
  minecart with it
  3. Link stations: /train link StationA StationB
  4. Travel: Ride your registered minecart to a station and select your
  destination

  ---
  Technical Info

  - Teleport cooldown: 10 seconds
  - Player detection radius: 20 blocks
  - Near command range: 500 blocks
  - Data: Auto-saved to YAML config
