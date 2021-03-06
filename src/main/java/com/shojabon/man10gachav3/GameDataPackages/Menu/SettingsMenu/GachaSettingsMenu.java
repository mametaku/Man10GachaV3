package com.shojabon.man10gachav3.GameDataPackages.Menu.SettingsMenu;

import com.shojabon.man10gachav3.DataPackages.GachaSound;
import com.shojabon.man10gachav3.DataPackages.SBannerItemStack;
import com.shojabon.man10gachav3.GamePackages.Man10GachaAPI;
import com.shojabon.man10gachav3.ToolPackages.SInventory;
import com.shojabon.man10gachav3.ToolPackages.SItemStack;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GachaSettingsMenu {
    Inventory inv;
    Listener listener;
    JavaPlugin plugin;
    String gacha;
    Player p;
    Man10GachaAPI api;

    private void reopenMenu(){
        new BukkitRunnable(){

            @Override
            public void run() {
                new GachaSettingsMenu(gacha, p);
            }
        }.runTaskLater(plugin, 1);
    }


    public GachaSettingsMenu(String gacha, Player p){
        p.closeInventory();
        this.gacha = gacha;
        this.p = p;
        this.api = new Man10GachaAPI();
        this.plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10GachaV3");
        inv = new SInventory(5, "§b§l" + gacha + "：設定メニュー").fillInventory(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayname(" ").build()).
        setItem(new int[]{12, 13, 14, 21,23,30,31,32}, new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayname(" ").build()).
        setItem(11, new SItemStack(Material.EMERALD).setDisplayname("§a§l§n価格設定").build()).
        setItem(15, new SItemStack(Material.LEGACY_SIGN).setDisplayname("§6§l§n看板設定").build()).
        setItem(22, new SItemStack(Material.CHEST).setDisplayname("§8§l§n§k00§7§l§nコンテナ設定§8§l§n§k00").setGlowingEffect(true).build()).
        setItem(29, new SItemStack(Material.NETHER_STAR).setDisplayname("§f§l§n一般設定").build()).
        setItem(33,new SItemStack(Material.DISPENSER).setDisplayname("§7§l§n統計データ").build()).
        setItem(44, new SItemStack(new SBannerItemStack((short) 4).pattern(new Pattern(DyeColor.WHITE, PatternType.STRIPE_LEFT)).pattern(new Pattern(DyeColor.WHITE, PatternType.STRIPE_TOP)).pattern(new Pattern(DyeColor.WHITE, PatternType.STRIPE_MIDDLE)).pattern(new Pattern(DyeColor.BLUE, PatternType.STRIPE_TOP)).pattern(new Pattern(DyeColor.BLUE, PatternType.STRIPE_BOTTOM)).pattern(new Pattern(DyeColor.BLUE, PatternType.CURLY_BORDER)).build()).setDisplayname("§c§l§n戻る").build()).build();
        listener = new Listener(p);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        p.openInventory(inv);
    }

    private void close(Player p){
        HandlerList.unregisterAll(listener);
        p.closeInventory();
    }

    class Listener implements org.bukkit.event.Listener
    {
        Player p;
        Listener(Player p){
            this.p = p;
        }

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getWhoClicked().getUniqueId() != p.getUniqueId()) return;
            e.setCancelled(true);
            if(e.getRawSlot() <= 44 && e.getRawSlot() != -999 && e.getInventory().getItem(e.getRawSlot()) != null) new GachaSound(Sound.BLOCK_DISPENSER_DISPENSE, 1 ,1).playSoundToPlayer((Player) e.getWhoClicked());
            new Thread(() -> {
                if(e.getRawSlot() == 44) new GachaSettingsSelectionMenu(p);
                if(e.getRawSlot() == 29) {
                    new GachaGeneralSettingsMenu(gacha, p, 0, 0,game -> {
                        if(game.getSettings().name.equalsIgnoreCase(gacha)){
                            api.updateGacha(api.getGacha(gacha));
                            reopenMenu();
                        }else{
                            new BukkitRunnable(){

                                @Override
                                public void run() {
                                    new GachaSettingsMenu(game.getSettings().name, p);
                                }
                            }.runTaskLater(plugin, 1);
                        }
                        return null;
                    });
                }
                if(e.getRawSlot() == 11){
                    new GachaPaymentSettingsMenu(gacha, p, event -> {
                        api.updateGacha(api.getGacha(gacha));
                        reopenMenu();
                        return null;
                    });
                }
                if(e.getRawSlot() == 22) {
                    new GachaContainerSettingsMenu(gacha, p, event -> {
                        api.updateGacha(api.getGacha(gacha));
                        reopenMenu();
                        return null;
                    });
                }
            }).start();
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getPlayer().getUniqueId() != p.getUniqueId()) return;
            close((Player) e.getPlayer());
        }


    }

}
