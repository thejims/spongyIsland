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

package io.github.kernegal.spongyisland.commands;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import io.github.kernegal.spongyisland.DataHolder;
import io.github.kernegal.spongyisland.utils.IslandManager;
import io.github.kernegal.spongyisland.utils.IslandPlayer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;

/**
 * Created by kernegal on 12/10/2016.
 */
public class IsSetHome implements CommandExecutor {
    private DataHolder data;
    private int protectionRadius;

    public IsSetHome(DataHolder data, int protectionRadius) {
        this.data = data;
        this.protectionRadius = protectionRadius;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Text.of(TextColors.RED, "Player only."));
            return CommandResult.success();
        }
        Player player = (Player) source;
        IslandPlayer playerData = data.getPlayerData(player.getUniqueId());

        UUID island = playerData.getIsland();
        if (island == null)
            return CommandResult.success();

        Vector3i islandLocation = data.getIslandLocation(island);
        Vector2i islandCoordinates = new Vector2i(islandLocation.getX(), islandLocation.getY());
        Vector2i min = islandCoordinates.sub(protectionRadius,protectionRadius);
        Vector2i max = islandCoordinates.add(protectionRadius,protectionRadius);
        Location<World> newLocation = player.getLocation();
        if(!newLocation.getExtent().getName().equals("world") ||
                newLocation.getX()<min.getX() || newLocation.getX()>=max.getX() ||
                newLocation.getZ()<min.getY() || newLocation.getZ()>=max.getY()){
            player.sendMessage(Text.of(TextColors.DARK_RED,"You need to be inside of your island"));
            return CommandResult.success();
        }

        data.setIslandHome(island, newLocation.getPosition().toInt());
        player.sendMessage(Text.of(TextColors.GREEN,"Island home updated!"));

        return CommandResult.success();
    }
}
