package com.chebab.weepingendermen;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginManager;

import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import org.bukkit.entity.EntityType;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WeepingEndermen extends JavaPlugin implements Listener
{
    private List<String> worlds;
    private Random rnd;

    public void onLoad() {
        this.getConfig().options().copyDefaults( true );
        this.saveConfig();
    }


    public void onEnable() {
        worlds = this.getConfig().getStringList( "worlds" );
        rnd = new Random();

        getServer().getPluginManager().registerEvents( this, this );

        System.out.println( "[Weeping Endermen] loaded, will effect:" );

        for( String world_name: worlds )
            System.out.print( " " + world_name );
        System.out.print( "\n" );
    }

    @EventHandler
    public void onEntityDamageByEntity( EntityDamageByEntityEvent event ) {
        Player victim;
        World world;
        // Something hit something check if the target is a player
        if( event.getEntityType() == EntityType.PLAYER && event.getDamager().getType() == EntityType.ENDERMAN ) {
            // Check that we are allowed to effect this world
            world = event.getEntity().getWorld();

            // TODO: Add a random thing here
            if( worlds.indexOf( world.getName() ) != -1 && rnd.nextInt(5) == 0 ) {
                victim = (Player)event.getEntity();
                randomSafeTeleport( victim );

                // Should we give the poor sod a confusion potioneffect?
                if( this.getConfig().getBoolean( "confuse", false ) ) {
                    PotionEffect pe = new PotionEffect( PotionEffectType.BLINDNESS, 100, 10 );
                    victim.addPotionEffect( pe, true );
                    PotionEffect pe2 = new PotionEffect( PotionEffectType.CONFUSION, 200, 5 );
                    victim.addPotionEffect( pe2, true );
                }

                else {
                    victim.sendMessage("*Poof*");
                }
            }
        }
    }

    private Location randomSafeTeleport(Player victim) {
        Location current = victim.getLocation();
        Location proposed = current.clone();
        double x, z, y;
        //boolean has_floor = false;
        boolean has_safespot = false;
        Block block, block_under;
        int safe_cnt = 0;
        World world = victim.getWorld();
        Chunk chunk;

        // Find a safe spot
        while( !has_safespot ) {
            if( rnd.nextBoolean() )
                x = (rnd.nextDouble() * 200);
            else
                x = -(rnd.nextDouble() * 200);

            if( rnd.nextBoolean() )
                z = (rnd.nextDouble() * 200);
            else
                z = -(rnd.nextDouble() * 200);

            y = (double)0.0;
            has_safespot = false;

            proposed = current.clone();
            proposed.add( x, y, z );
            proposed.setY( (double)world.getMaxHeight() - 3.0 ); // Set it so we don't get out of sync..

            for( double y2 = proposed.getY(); y2 >= (double)5.0; y2-- ) {
                proposed.subtract( (double)0.0, (double)1.0, (double)0.0 );

                block = proposed.getBlock();

                if( block.getType() == Material.AIR )
                {
                    block_under = block.getRelative(BlockFace.DOWN);

                    if( block_under.getType() == Material.AIR ) {
                        block_under = block_under.getRelative(BlockFace.DOWN);

                        if( block_under.getType() != Material.AIR && block_under.getType() != Material.LAVA ) {
                            //victim.sendMessage("Good we found a spot on solid " + block_under.getType() );
                            //victim.sendMessage("At x:" + proposed.getX() + " y:" + y2 + " z:" + proposed.getZ());
                            proposed.subtract( (double)0.0, (double)1.0, (double)0.0 ); // So we get in the the tested region.
                            has_safespot = true;
                            break;
                        }
                    }
                }

            }
        }

        // If needed load the chunk (just to be safe).
        chunk = proposed.getChunk();
        if( !chunk.isLoaded() )
            chunk.load();

        victim.teleport( proposed );
        return proposed;
    }
}
