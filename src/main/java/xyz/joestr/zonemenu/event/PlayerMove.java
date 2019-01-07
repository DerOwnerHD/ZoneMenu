/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.joestr.zonemenu.event;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.bukkit.boss.BarColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import xyz.joestr.zonemenu.ZoneMenu;
import xyz.joestr.zonemenu.util.ZoneMenuToolType;

/**
 *
 * @author Joel
 */
public class PlayerMove implements Listener {

    private ZoneMenu plugin;

    public PlayerMove(ZoneMenu zonemenu) {

        this.plugin = zonemenu;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        if (!this.plugin.zoneMenuPlayers.containsKey(event.getPlayer())) {
            return;
        }

        if (!this.plugin.zoneMenuPlayers.get(event.getPlayer()).getToolType().equals(ZoneMenuToolType.FIND)) {
            return;
        }

        CompletableFuture.supplyAsync(
            () -> {
                RegionQuery regionQuery
                = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();

                ApplicableRegionSet applicableRegions = regionQuery.getApplicableRegions(BukkitAdapter.adapt(event.getTo()));

                List<String> regionNames = new ArrayList<>();

                applicableRegions.forEach((region) -> regionNames.add(region.getId().replace("+", "#").replace("-", ".")));

                String regions = regionNames.stream().collect(Collectors.joining(", "));

                if (regions.isEmpty()) {
                    this.plugin.zoneMenuPlayers.get(event.getPlayer()).getZoneFindBossbar().setColor(BarColor.GREEN);
                    this.plugin.zoneMenuPlayers.get(event.getPlayer()).getZoneFindBossbar().setTitle(
                        this.plugin.colorCode('&', (String) this.plugin.configDelegate.getMap().get("event_find_no")));
                } else if (applicableRegions.size() > 1) {
                    this.plugin.zoneMenuPlayers.get(event.getPlayer()).getZoneFindBossbar().setColor(BarColor.RED);
                    this.plugin.zoneMenuPlayers.get(event.getPlayer()).getZoneFindBossbar().setTitle(
                        this.plugin.colorCode('&',
                            ((String) this.plugin.configDelegate.getMap().get("event_find_multi")).replace("{ids}", regions)));
                } else {
                    this.plugin.zoneMenuPlayers.get(event.getPlayer()).getZoneFindBossbar().setColor(BarColor.RED);
                    this.plugin.zoneMenuPlayers.get(event.getPlayer()).getZoneFindBossbar().setTitle(
                        this.plugin.colorCode('&',
                            ((String) this.plugin.configDelegate.getMap().get("event_find")).replace("{ids}", regions)));
                }

                return true;
            }
        );
    }
}
