package com.chebab.weepingendermen;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;
import org.bukkit.World;
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
        // Something hit something check if the target is a player
        if( event.getEntityType() == EntityType.PLAYER && event.getDamager().getType() == EntityType.ENDERMAN ) {
            // TODO: Add a random thing here
            if( rnd.nextInt(5) == 0 || true ) {
                victim = (Player)event.getEntity();
                randomSafeTeleport( victim );
                victim.sendMessage("*Poof*");
            }
        }
    }

    private Location randomSafeTeleport(Player victim) {
        Location current = victim.getLocation();
        Location proposed = current.clone();
        double x, z, y;
        boolean has_floor = false;
        Block block;
        int safe_cnt = 0;

        // Find a safe spot
        while( safe_cnt < 3 && !has_floor ) {
            x = (rnd.nextDouble() * 200) - 100;
            z = (rnd.nextDouble() * 200) - 100;
            y = (double)-1.0;
            has_floor = false;
            // victim.sendMessage("Checking if " + x + "x" + z + " is any good");
            proposed.add( x, y, z );

            for( double x2 = proposed.getY(); x2<=254.0; x2++ ) {
                proposed.add( (double)0.0, (double)1.0, (double)0.0 );
                // victim.sendMessage("  At " + proposed.getY() );
                block = proposed.getBlock();

                if( block.getType() == Material.AIR &&
                    block.getRelative(BlockFace.DOWN).getType() != Material.LAVA ) {
                    safe_cnt++;
                    // System.out.println("Good " + safe_cnt );
                }
                else if( block.getType() != Material.AIR && block.getType() != Material.LAVA ) {
                    // So we don't drop people into bottomless pits
                    // System.out.println( "found a floor" );
                    safe_cnt = 0;
                    has_floor = true;
                }
                else {
                    safe_cnt = 0;
                    has_floor = false;
                }

                if( safe_cnt >= 3 )
                    break;
            }

            // Try to find a floor
            if ( !has_floor ) {
                // victim.sendMessage("Looking for floor");
                safe_cnt = 0;
                proposed.setY( current.getY() );
                for( double x2 = proposed.getY(); x2>=4.0; x2-- ) {
                    proposed.subtract( (double)0.0, (double)1.0, (double)0.0 );
                    // victim.sendMessage("  At " + proposed.getY() );
                    block = proposed.getBlock();

                    if( block.getType() == Material.AIR &&
                        block.getRelative(BlockFace.DOWN).getType() != Material.LAVA ) {
                        safe_cnt++;
                        // victim.sendMessage("Good " + safe_cnt );
                    }
                    else if( block.getType() != Material.AIR && block.getType() != Material.LAVA && safe_cnt >= 3) {
                        //So we don't drop people into bottomless pits
                        safe_cnt++;
                        has_floor = true;
                    }
                    else {
                        safe_cnt = 0;
                        has_floor = false;
                    }

                    if( safe_cnt >= 3 && has_floor ) {
                        proposed.add((double)0.0, (double)6.0, (double)0.0 ); // hack to avoid the fix below...
                        break;
                    }
                }
            }

        }

        proposed.subtract( (double)0.0, (double)3.0, (double)0.0 );
        victim.teleport( proposed );
        return proposed;
    }
}
