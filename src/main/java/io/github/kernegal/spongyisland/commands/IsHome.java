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

import io.github.kernegal.spongyisland.DataHolder;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;


import javax.annotation.Nonnull;
import java.util.Optional;


public class IsHome implements CommandExecutor {
    private DataHolder data;

    public IsHome(DataHolder data) {
        this.data = data;
    }

    @Override
    @Nonnull
    public CommandResult execute(@Nonnull CommandSource source, @Nonnull CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Text.of(TextColors.RED, "Player only."));
            return CommandResult.success();
        }

        Player player = (Player) source;
        Optional<Player> friend = args.getOne("friend");
        if (friend.isPresent()) {
            String friendsIsland = data.getPlayersIsland(friend.get().getUniqueId().toString());
            String playerID = player.getUniqueId().toString();
            if (friendsIsland == null) {
                source.sendMessage(Text.of(TextColors.DARK_RED,friend.get().getName(), " does not have an island"));
                return CommandResult.success();
            }
            if (data.isIslandFriend(friendsIsland, playerID)) {
                data.teleportPlayerToIsland(player, friendsIsland);
                return CommandResult.success();
            } else {
                source.sendMessage(Text.of(TextColors.DARK_RED,"Your are not a friend on ",friend.get().getName(), "'s island"));
            }
        } else {
            data.teleportPlayerToHome(player);
        }

        return CommandResult.success();
    }
}
