package me.redslime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;


public class Mace extends JavaPlugin implements Listener {
    public boolean enabled;
    public boolean craftingrecipe;
    public int maxdmg;

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    
    public void setConfig(){
        this.enabled = getConfig().getBoolean("enabled", true);
        this.maxdmg = getConfig().getInt("max-damage", 0);
        this.craftingrecipe = getConfig().getBoolean("crafting-recipe", true);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Mace Backport has been enabled!");
        setConfig();
        getCommand("givemace").setExecutor(new GiveMace());
        getCommand("reloadmace").setExecutor(new ReloadMace());

        if(craftingrecipe && enabled){
            ItemStack mace = new ItemStack(Material.WOODEN_AXE, 1);
            ItemMeta meta = mace.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + "Mace");
            meta.setLore(List.of("Mace Backported!", ChatColor.LIGHT_PURPLE + "Press Right click to launch yourself!"));
            mace.setItemMeta(meta);

            NamespacedKey key = new NamespacedKey(this, "mace_recipe");
            ShapedRecipe recipe = new ShapedRecipe(key, mace);

            recipe.shape("ONO", "OMO", "OIO");

            recipe.setIngredient('O', Material.DIAMOND_BLOCK);
            recipe.setIngredient('N', Material.HEART_OF_THE_SEA);
            recipe.setIngredient('M', Material.ENCHANTED_GOLDEN_APPLE);
            recipe.setIngredient('I', Material.ELYTRA);

            Bukkit.addRecipe(recipe);
        }
    }

    @EventHandler
    public  void onEntityHit(EntityDamageByEntityEvent event){
        if(enabled){
            if (event.getDamager() instanceof Player player){
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item != null){
                    if (item.hasItemMeta()){
                        ItemMeta meta = item.getItemMeta();
                        if (meta.hasLore()){
                            if (meta.getLore().contains("Mace Backported!") && item.getType() == Material.WOODEN_AXE){
                                double damage;
                                if (maxdmg != 0 && player.getFallDistance() != 0) {
                                    damage = Math.max(7, (Math.min(maxdmg, (player.getFallDistance()))));
                                } else {
                                    damage = Math.max(7, (player.getFallDistance()));
                                }
                                // player.sendMessage(String.valueOf(player.getFallDistance()));
                                player.setFallDistance(0);
                                event.setDamage(damage);
                            }
                        }
                    }   
                }
            }
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event){
        if(enabled) {
            ItemStack item = event.getItem();
            if (item != null){
                if (item.hasItemMeta()){
                    ItemMeta meta = item.getItemMeta();
                    if (meta.hasLore()){
                        if (meta.getLore().contains("Mace Backported!") && item.getType() == Material.WOODEN_AXE){
                            // This is a workaround to my other plugin, NoIllegals.
                            Damageable dmg = (Damageable) item.getItemMeta();
                            dmg.setDamage(0);
                            item.setItemMeta(dmg);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack item = event.getItem();
            if (item != null && enabled){
                ItemMeta meta = item.getItemMeta();
                if(meta != null){
                    if(meta.hasLore()) {
                        if(meta.getLore().contains("Mace Backported!")){
                            if(cooldowns.containsKey(player.getUniqueId())){
                                Long playercooldown = cooldowns.get(player.getUniqueId());
                                if(playercooldown > System.currentTimeMillis()){
                                    player.sendMessage(ChatColor.RED + "You have " + String.valueOf((playercooldown - System.currentTimeMillis())/1000) + "s left!");
                                    return;
                                } else {
                                    cooldowns.remove(player.getUniqueId());
                                }
                            }

                            if(!cooldowns.containsKey(player.getUniqueId())){
                                Vector vec = player.getVelocity();
                                vec.setY(5);

                                event.getPlayer().setVelocity(vec);
                                cooldowns.put(player.getUniqueId(), System.currentTimeMillis()+5000);
                            }
                        }
                    }
                }
            }
        }
    }

    public class ReloadMace implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
            if(sender.hasPermission("mace.reload")){
                reloadConfig();
                setConfig();

                if(craftingrecipe == false) {
                    NamespacedKey key = new NamespacedKey(Mace.this, "mace_recipe");
                    Recipe recipe = Bukkit.getRecipe(key);
                    if(recipe != null) {
                        Bukkit.removeRecipe(key);
                    }
                }
                sender.sendMessage(ChatColor.GREEN + "Reloaded Mace Config!");
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission!");
            }
            return true;
        }
    }

    public class GiveMace implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

            Player target;

            if (sender instanceof Player player){
                if (!player.hasPermission("mace.give")){
                    player.sendMessage(ChatColor.RED + "You do not have permission!");
                    return true;
                }
            }


            if (args.length != 0){
                target = Bukkit.getPlayer(args[0]);
                if (target == null){
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                } 
            } else {
                if (!(sender instanceof Player)){
                    sender.sendMessage(ChatColor.RED + "You need to specify a player!");
                    return true;
                }
                target = (Player) sender;
            }

            ItemStack mace = new ItemStack(Material.WOODEN_AXE, 1);
            ItemMeta meta = mace.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + "Mace");
            meta.setLore(List.of("Mace Backported!", ChatColor.LIGHT_PURPLE + "Press Right click to launch yourself!"));
            mace.setItemMeta(meta);

            target.getInventory().addItem(mace);
            if((Player) sender == target){
                sender.sendMessage(ChatColor.GREEN + "You now have a mace!");
            } else {
                sender.sendMessage(ChatColor.GREEN + "You have given a mace to " + target.getName());
                target.sendMessage(ChatColor.GREEN + "You now have a mace!");
            }

            return true;
        }
    }
    @Override
    public void onDisable() {
        getLogger().info("Mace has been disabled!");
    }
    
}