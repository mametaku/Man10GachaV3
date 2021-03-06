package com.shojabon.man10gachav3.GameDataPackages.Menu.SettingsMenu;

import com.shojabon.man10gachav3.DataPackages.GachaBannerDictionary;
import com.shojabon.man10gachav3.DataPackages.GachaItemStack;
import com.shojabon.man10gachav3.DataPackages.GachaSound;
import com.shojabon.man10gachav3.GamePackages.GachaGame;
import com.shojabon.man10gachav3.GamePackages.Man10GachaAPI;
import com.shojabon.man10gachav3.ToolPackages.MultiItemStackSelectorAPI;
import com.shojabon.man10gachav3.ToolPackages.SInventory;
import com.shojabon.man10gachav3.ToolPackages.SItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class GachaContainerSettingsMenu {
    Inventory inv;
    Listener listener = new Listener();
    JavaPlugin plugin;
    String gacha;
    Player p;
    int currentPage = 0;
    SInventory sInventory;
    int[] slots = new int[]{10,14,19,23,28,32,37,41};
    int[] settingsSlots = new int[]{11,15,20,24,29,33,38,42};
    int[] deleteSlots = new int[]{12,16,21,25,30,34,39,43};
    GachaContainerSettingsMenu menu;

    GachaGame game;
    GachaBannerDictionary dict = new GachaBannerDictionary();

    Function<InventoryClickEvent, String> cancelFunction;
    Man10GachaAPI api = new Man10GachaAPI();

    boolean movingMenu = false;

    public GachaContainerSettingsMenu(String gacha, Player p, Function<InventoryClickEvent, String> cancelFunction){
        p.closeInventory();
        this.gacha = gacha;
        this.cancelFunction = cancelFunction;
        menu = this;
        game = Man10GachaAPI.gachaGameMap.get(gacha);
        this.p = p;
        this.plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10GachaV3");
        Bukkit.getPluginManager().registerEvents(listener, plugin);

        SInventory inve = new SInventory(6, "§b§l" + gacha + "§7§lコンテナ設定");
        inve.fillInventory(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayname(" ").build());
        inve.setItem(slots, new SItemStack(Material.BARRIER).setDisplayname("§c§lなし").build());
        inve.setItem(new int[]{11,15,20,24,29,33,38,42}, new SItemStack(Material.ANVIL).setDisplayname("§7§l設定").build());
        inve.setItem(new int[]{12,16,21,25,30,34,39,43}, new SItemStack(Material.TNT).setDisplayname("§4§l消去する").build());
        inve.setItem(new int[]{18,27}, new SItemStack(Material.GLASS_PANE).setDisplayname("戻る").setDamage(14).build());
        inve.setItem(new int[]{26,35}, new SItemStack(Material.GLASS_PANE).setDisplayname("次へ").setDamage(14).build());
        inve.setItem(53, dict.getSymbol("back"));
        inve.setItem(49, new SItemStack(Material.HOPPER).setDisplayname("§6§lアイテムをインポートする").build());
        sInventory = inve;
        inv = inve.build();
        render(currentPage);
        p.openInventory(inv);
    }

    private void reopen(){
        new BukkitRunnable(){

            @Override
            public void run() {
                new GachaContainerSettingsMenu(gacha, p, cancelFunction);
            }
        }.runTaskLater(plugin, 2);
    }

    private void close(Player p){
        HandlerList.unregisterAll(listener);
        p.closeInventory();
    }

    private void render(int menu){
        int len = slots.length;
        for(int i =0; i < slots.length; i++){
            inv.setItem(slots[i], new SItemStack(Material.BARRIER).setDisplayname("§c§lなし").build());
        }
        if(game.getItemIndex().size() <= len) {
            len = game.getItemIndex().size();
        }
        for(int i =0; i < len; i++){
            if(game.getItemIndex().size() > menu * 8 + i){
                inv.setItem(slots[i], new SItemStack(game.getItemIndex().get(menu * 8 + i).item).setAmount(game.getItemIndex().get(menu * 8 + i).amount).build());
                //アイテムセット
            }else{
            }
        }
    }

    class Listener implements org.bukkit.event.Listener
    {

        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getWhoClicked().getUniqueId() != p.getUniqueId()) return;
            e.setCancelled(true);
            if(e.getRawSlot() <= 53 && e.getRawSlot() != -999 && e.getInventory().getItem(e.getRawSlot()) != null) new GachaSound(Sound.BLOCK_DISPENSER_DISPENSE, 1 ,1).playSoundToPlayer((Player) e.getWhoClicked());
            if(e.getRawSlot() == 26 || e.getRawSlot() == 35){

                currentPage += 1;
                render(currentPage);

                return;
            }
            if(e.getRawSlot() == 49){
                movingMenu = true;
                new MultiItemStackSelectorAPI(p, new ArrayList<>(), (event, itemStacks) -> {
                    HashMap<String, Integer> itemMap = new HashMap<>();
                    for(ItemStack item : itemStacks){
                        if(itemMap.containsKey(new SItemStack(item).toBase64())){
                            itemMap.put(new SItemStack(item).toBase64(), itemMap.get(new SItemStack(item).toBase64()) + 1);
                        }else{
                            itemMap.put(new SItemStack(item).toBase64(), 1);
                        }
                    }
                    for(String key : itemMap.keySet()){
                        ItemStack item = new SItemStack(key).build();
                        int index = menu.game.getItemIndex().size();
                        GachaItemStack gItemStack = new GachaItemStack(item);
                        gItemStack.amount = item.getAmount();
                        menu.game.setItemIndex(-1, gItemStack);
                        menu.game.setStorageAmount(index, itemMap.get(key));
                    }
                    reopen();
                    return null;
                }, event -> {
                    reopen();
                    return null;
                });
                return;
            }
            if(e.getRawSlot() == 18 || e.getRawSlot() == 27){
                currentPage -= 1;
                if(currentPage < 0) currentPage = 0;
                render(currentPage);
                return;
            }
            if(e.getRawSlot() == 53) {
                close(p);
                cancelFunction.apply(e);
                return;
            }
            for(int i =0; i < settingsSlots.length; i++){
                if(e.getRawSlot() == deleteSlots[i]){
                    int itemSlot = e.getRawSlot() - 2;
                    int index = getIndexOf(slots, itemSlot) + 8 * currentPage;
                    if(game.getItemIndex().size() >= index){
                        game.setStorageAmount(index, 0);
                        game.getItemIndex().remove(index);
                        render(currentPage);
                    }else{
                        return;
                    }
                }
                if(e.getRawSlot() == settingsSlots[i]){
                    int itemslot = e.getRawSlot() - 1;
                    int index = getIndexOf(slots, itemslot) + 8 * currentPage;
                    if(game.getItemIndex().size() >= index){
                        movingMenu = true;
                        new GachaItemStackSettingsMenu(menu, 0,0,index,event -> {
                            p.closeInventory();
                            Bukkit.getPluginManager().registerEvents(listener, plugin);
                            api.updateGacha(game);
                            reopen();
                            return null;
                        });
                    }else{
                        movingMenu = true;
                        new GachaItemStackSettingsMenu(menu, 0,0,index,event -> {
                            p.closeInventory();
                            Bukkit.getPluginManager().registerEvents(listener, plugin);
                            api.updateGacha(game);
                            reopen();
                            return null;
                        });
                    }
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(e.getPlayer().getUniqueId() != p.getUniqueId()) return;
            close((Player) e.getPlayer());
            if(!movingMenu){
                cancelFunction.apply(null);
            }
        }

    }

    public int getIndexOf(int[] arr, int number){
        for(int i =0; i < arr.length; i++){
            if(arr[i] == number){
                return i;
            }
        }
        return -1;
    }
}
