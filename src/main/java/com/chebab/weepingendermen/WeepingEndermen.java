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
            if( rnd.nextInt(5) == 0 ) {
                victim = (Player)event.getEntity();
                victim.teleport( randomSafeTeleport( victim.getLocation() ) );
                victim.sendMessage("*Poof*");
            }
        }
    }

    private Location randomSafeTeleport(Location current) {
        Location proposed = current.clone();
        double x, z, y;
        boolean safe = false;
        Block block;
        int safe_cnt = 0;
        
        // Find a safe spot
        while( safe_cnt < 3 ) {
            x = (rnd.nextDouble() * 200) - 100;
            z = (rnd.nextDouble() * 200) - 100;
            y = (double)-1.0;
            //System.out.println("Checking if " + x + "x" + z + " is any good");
            proposed.add( x, y, z );
            
            for( double x2 = x; x<=254.0; x++ ) {
                proposed.add( (double)0.0, (double)1.0, (double)0.0 );
                //System.out.println("  At " + proposed.getY() );
                block = proposed.getBlock();
                
                if( block.getType() == Material.AIR &&
                    block.getRelative(BlockFace.DOWN).getType() != Material.LAVA ) {
                    safe_cnt++;
                    //System.out.println("Good " + safe_cnt );
                }
                else {
                    safe_cnt = 0;
                }
                
                if( safe_cnt == 3 )
                    break;
            }
        }

        proposed.subtract( (double)0.0, (double)3.0, (double)0.0 );

        return proposed;
    }
}
