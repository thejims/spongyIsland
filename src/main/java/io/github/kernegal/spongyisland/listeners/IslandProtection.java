/*
 * This file is part of the plugin SopngyIsland
 *
 * Copyright (c) 2016 kernegal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.github.kernegal.spongyisland.listeners;

import io.github.kernegal.spongyisland.DataHolder;
import io.github.kernegal.spongyisland.SpongyIsland;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * Created by kernegal on 12/10/2016.
 */
public class IslandProtection {

    private DataHolder data;

    public IslandProtection(DataHolder data) { this.data = data; }

    @Listener
    @IsCancelled(Tristate.FALSE)
    @Include({ChangeBlockEvent.Break.class,ChangeBlockEvent.Place.class})
    public void blockChangeEvent(ChangeBlockEvent event, @First Player player) {
        if(player.hasPermission(SpongyIsland.pluginId+".islands.modify_blocks"))
            return;

        if(!event.getTargetWorld().getName().equals("world"))
            return;

        for (Transaction<BlockSnapshot> transaction: event.getTransactions()) {
            // Look up the island located at the block transaction location
            String island = data.findIslandAtCoordinates(transaction.getOriginal().getLocation().get().getPosition().toInt());

            // Block location not found on an island
            // Player doesn't have permission on the island
            if (island == null || !data.isIslandFriend(island, player.getUniqueId().toString()))
                transaction.setValid(false);
        }
    }

    @Listener
    @Exclude(InteractBlockEvent.Primary.class)
    public void onInteract(InteractBlockEvent event, @Root Player player) {
        if(player.hasPermission(SpongyIsland.pluginId+".islands.modify_blocks"))
            return;

        Optional<World> world = Sponge.getServer().getWorld(event.getTargetBlock().getWorldUniqueId());
        if(!world.isPresent() || !world.get().getName().equals("world"))
            return;

        // Look up the island located at the block transaction location
        String island = data.findIslandAtCoordinates(event.getTargetBlock().getPosition().toInt());

        // Block location not found on an island or
        // Player doesn't have permission on the island
        if (island == null || !data.isIslandFriend(island, player.getUniqueId().toString()))
            event.setCancelled(true);
    }

    /*
     * Easily doable, but is it worth it in the lag it might cause? perhaps Async this..?
    @Listener
    @Exclude({MoveEntityEvent.Teleport.class,MoveEntityEvent.Teleport.Portal.class})
    public void onEvent(MoveEntityEvent event, @Getter("getTargetEntity") Player player) {
        IslandPlayer playerData = data.getPlayerData(player.getUniqueId());

        if (event.getFromTransform().getExtent().getName().equals("world")){
            Vector2i islandCoordinates=playerData.getIsPosition().mul(islandRadius*2);
            Vector2i min = islandCoordinates.sub(protectionRadius,protectionRadius);
            Vector2i max = islandCoordinates.add(protectionRadius,protectionRadius);


            Vector3i from = event.getFromTransform().getPosition().toInt();
            Vector3i to = event.getToTransform().getPosition().toInt();


            if((from.getX()<min.getX() || from.getX()>=max.getX() ||
                    from.getZ()<min.getY() || from.getZ()>=max.getY()) &&
                    !(to.getX()<min.getX() || to.getX()>=max.getX() ||
                            to.getZ()<min.getY() || to.getZ()>=max.getY())){
                player.sendMessage(Text.of(TextColors.GREEN, "Entering your island"));

            }
            else if(!(from.getX()<min.getX() || from.getX()>=max.getX() ||
                    from.getZ()<min.getY() || from.getZ()>=max.getY()) &&
                    (to.getX()<min.getX() || to.getX()>=max.getX() ||
                            to.getZ()<min.getY() || to.getZ()>=max.getY())){
                player.sendMessage(Text.of(TextColors.RED, "leaving your island"));

            }
        }
    }
    */

    @Listener
    public void onPortalTP(MoveEntityEvent.Teleport.Portal event, @Getter("getTargetEntity") Player player) {
        SpongyIsland.getPlugin().getLogger().info(event.getFromTransform().getExtent().getName());

        if(event.getFromTransform().getExtent().getName().equals("DIM-1")){
            event.setCancelled(true);
            player.sendMessage(Text.of("Use island command to get to your island or create a new one"));

        }
        else if(event.getToTransform().getExtent().getName().equals("DIM-1")){
            PortalAgent portalAgent = event.getPortalAgent();
            Optional<Location<World>> orCreatePortal = portalAgent.findOrCreatePortal(event.getToTransform().getExtent().getLocation(0, 0, 0));
            if(orCreatePortal.isPresent()){
                event.setToTransform(event.getToTransform().setLocation(orCreatePortal.get()));
            }
            else{
                player.sendMessage(Text.of(TextColors.DARK_RED,"Can't go to Nether"));
                event.setCancelled(true);
            }

        }
    }

}


