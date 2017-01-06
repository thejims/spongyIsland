package io.github.kernegal.spongyisland.commands;

import io.github.kernegal.spongyisland.DataHolder;
import io.github.kernegal.spongyisland.utils.IslandPlayer;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;


import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Created by Bebabo10 on 1/1/2017.
 */
public class IsFriend implements CommandExecutor {
    private DataHolder data;

    public IsFriend(DataHolder data) { this.data = data; }

    @Override
    @Nonnull
    public CommandResult execute(@Nonnull CommandSource source, @Nonnull CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Text.of(TextColors.RED, "Player only."));
            return CommandResult.success();
        }
        if(!args.hasAny("friend") || !args.hasAny("action")) {
            source.sendMessage(Text.of(TextColors.RED, "Invalid arguments"));
            return CommandResult.success();
        }

        String action = args.<String>getOne("action").get();
        User user = args.<User>getOne("friend").get();
        String player = user.getUniqueId().toString();
        String island = data.getPlayersIsland(player);
        if (action.equals("add")) {
            if (data.addIslandFriend(island, player)) {
                source.sendMessage(Text.of(TextColors.GREEN,"Added " + user.getName() + " as an island friend."));
            } else {
                source.sendMessage(Text.of(TextColors.DARK_RED,"Unable to add " + user.getName() + " as an island friend."));
            }
        } else if(action.startsWith("rem")) {
            if (data.removeIslandFriend(island, player)) {
                source.sendMessage(Text.of(TextColors.GREEN,"Removed " + user.getName() + " as an island friend."));
            } else {
                source.sendMessage(Text.of(TextColors.DARK_RED,"Unable to remove " + user.getName() + " as an island friend."));
            }
        }
        return CommandResult.success();
    }
}
