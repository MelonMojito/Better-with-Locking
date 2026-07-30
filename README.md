# Better with Locking

Container locking for [Better than Adventure](https://betterthanadventure.net/) servers. 

Lets players lock chests, furnaces, blast furnaces, dispensers, trommels, baskets, activators, and golden meshes so only they (and players they trust) can open, break, pick up, or piston-push them.

## Commands

All commands operate on the container you are looking at (unless noted otherwise).

| Command | Description |
| --- | --- |
| `/lock` | Lock the container. Double chests lock as one. |
| `/unlock` | Unlock the container (owner or bypass only). Clears the trust list and community flag. |
| `/lock info` | Show the owner, community status, and trusted players of the container. |
| `/lock trust <player>` | Trust a player to this container. |
| `/lock untrust <player>` | Remove a player's trust from this container. |
| `/lock trustall <player>` | Trust a player to **all** of your containers. |
| `/lock untrustall <player>` | Remove a player from your trust-all list. |
| `/lock trustcommunity` | Let everyone open this container (but not break or pick it up). |
| `/lock untrustcommunity` | Revoke community access. |
| `/lock toggle onblockplaced` | Toggle automatically locking containers you place. **On by default.** |
| `/lock toggle onblockpunched` | Toggle locking containers by punching them. |
| `/lock toggle feedback` | Toggle all lock text/sound feedback (except `/lock info`). **On by default.** |
| `/lock toggle bypass` | (Admin) Toggle bypassing all locks. |

## Notes

- Lock data is stored on the tile entity itself (in the world save); per-player settings are stored in `config/betterwithlocking/users/`.
- User files from MelonUtilities can be copied into `config/betterwithlocking/users/` - the lock data format is unchanged.
- The mod is server-side; clients do not need it installed.
