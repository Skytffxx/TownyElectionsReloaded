# TownyElectionsReloaded Changelog

Fork of [TownyElections](https://github.com/aurgiyalgo/TownyElections) by **aurgiyalgo**, updated and maintained by **skyttffxx**.

---

## v2.0 — Paper 1.21 Port, Bug Fix & Rebrand Release

### Branding
- Plugin renamed **TownyElectionsReloaded** (fork identity, no conflict with original)
- Package moved from `me.lofienjoyer.TownyElections` → `me.skyttffxx.townyelectionsreloaded`
- Maven groupId/artifactId updated accordingly; `Built-By` set to `skyttffxx`
- `plugin.yml` `authors` field lists `[skyttffxx, aurgiyalgo]` to credit original work
- In-game `/telect info` now shows maintainer and credits original author

### Build / Dependency Changes
- **Replaced** `spigot-api 1.19.2` with `paper-api 1.21.1-R0.1-SNAPSHOT` (PaperMC repo)
- **Removed** Spigot repository; added `https://repo.papermc.io/repository/maven-public/`
- **Updated** Towny dependency `0.98.3.0` → `0.100.2.0`
- **Raised** Java compile target from 17 → 21 (required by Paper 1.21 and Towny 0.100.x)
- **Updated** `api-version` in `plugin.yml`: `1.19` → `1.21`

### API Migration (Towny)
- **Removed all `getDataSource()` calls** — this API was removed in Towny 0.99.5+:
  - `TownElection.setup()`: `getDataSource().getTown(UUID)` → `TownyUniverse.getInstance().getTown(UUID)`
  - `NationElection.setup()`: `getDataSource().getNation(UUID)` → `TownyUniverse.getInstance().getNation(UUID)`
  - `TownParty.setup()`: same replacement; removed dead try/catch on `NotRegisteredException`
  - `NationParty.setup()`: same replacement; removed dead try/catch
  - `TownElection.finishElection()`: removed `getDataSource().saveTown(town)` (Towny manages its own saves)
  - `NationElection.finishElection()`: removed `getDataSource().saveNation(nation)`
- **Replaced name-based resident lookups** with UUID-based `TownyUniverse.getResident(UUID)`:
  - `TownElection.finishElection()`: `getResident(OfflinePlayer.getName())` → `getResident(party.getLeader())` (direct UUID)
  - `NationElection.finishElection()`: same
  - `ElectionManager.getTownElection(Player)` and `getNationElection(Player)`
  - `TownVoteGui`, `NationVoteGui`
  - `PartyCreateSubCommand`, `PartyAcceptSubCommand`, `PartyInvitesSubCommand`
- **Replaced `Town/Nation.hasResident(String)`** (deprecated) with `hasResident(Resident)` in all broadcast helpers

### Bug Fixes
1. **Listeners never registered** (`TownyElections.java`) — `TEListener` was instantiated but never passed to `Bukkit.getPluginManager().registerEvents()`. No Towny or player events were ever handled. Fixed by adding the registration call in `onEnable()`.
2. **NPE on offline player leaving town** (`TEListener.onPlayerTownLeave`) — `Bukkit.getPlayer(name)` returns `null` for offline players; calling `.getUniqueId()` on `null` crashed the server. Fixed by obtaining UUID directly from the `Resident` object and null-checking the `Player` before election-vote lookups.
3. **Nation election list showed town parties** (`ElectionsListSubCommand`) — `getPartiesForTown(nation.getName())` was called instead of `getPartiesForNation(nation.getName())`, returning an empty list for every nation election listing.
4. **Nation convoke: missing `return true` after active-election guard** (`ElectionsConvokeSubCommand`) — when a nation election was already active, the message was sent but execution fell through and started a second election anyway. Added the missing `return true`.
5. **Debug print statement left in production** (`ElectionsVoteSubCommand`) — `System.out.println(Arrays.toString(args))` was printed to the server console on every `/elections vote` invocation. Removed.
6. **Party duplicate-name check used wrong arg index** (`PartyCreateSubCommand`) — town-party name was compared against `args[2]` (out of bounds for a 2-argument command) instead of `args[1]`. Fixed; also unified both branches to use `equalsIgnoreCase` and `anyMatch`.
7. **`/townyelections reload` crashed for console sender** (`TElectCommandHandler`) — `(Player) sender` was cast unconditionally before an instanceof check, throwing `ClassCastException` when the console ran the command. Fixed with a proper instanceof guard.
8. **Reload command did not send confirmation** (`TElectCommandHandler`) — `getString("plugin-reloaded")` was called and the return value discarded; the reloading party received no feedback. Fixed by routing through `sender.sendMessage(...)`.
9. **`already-part-of-a-party` key did not exist** (`PartyCreateSubCommand`, town branch) — looked up a missing language key, silently showing the prefix with no message text. Changed to use the existing `already-in-a-party` key (already used correctly in the nation branch).
10. **Party info listed wrong assistants** (`PartyInfoSubCommand`) — the loop appended `party.getAssistants().get(0)` (always index 0) instead of `get(i)`, and the first assistant was printed as a raw UUID rather than a player name. Fixed both loops to use the correct index and `Bukkit.getOfflinePlayer(...).getName()`.
11. **`DataHandler.saveData()` NullPointerException on first run** (`DataHandler`) — when the plugin data folder did not exist at construction time, `_dataFile` was left `null`; the first `saveData()` call then threw an NPE. Fixed by always initialising `_dataFile` in the constructor and creating the parent directory inside `saveData()` if needed.
12. **`DataHandler` used unclosed `FileWriter`** — `FileWriter` was not wrapped in try-with-resources, so a write error could leave the file handle open. Replaced with a try-with-resources block.
13. **Language key typo** (`language.yml`) — `player-joined-the party:` (missing hyphen) → `player-joined-the-party:`.
14. **`net.md_5.bungee.api.ChatColor` import in GUI files** (`TownVoteGui`, `NationVoteGui`) — BungeeCord's `ChatColor` is not present on Paper 1.21 without the legacy compat layer. Replaced with `org.bukkit.ChatColor`.
