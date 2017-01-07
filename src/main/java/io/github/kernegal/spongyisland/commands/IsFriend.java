package io.github.kernegal.spongyisland.commands;

import io.github.kernegal.spongyisland.DataHolder;
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
        Player player = (Player)source;
        String action = args.<String>getOne("action").get();
        Player friend = args.<Player>getOne("friend").get();
        String friendID = friend.getUniqueId().toString();
        String islandID = data.getPlayersIsland(player.getUniqueId().toString());
        if (islandID == null) {
            source.sendMessage(Text.of(TextColors.DARK_RED,"You do not have an island to add any friends to!"));
        } else if (action.equals("add")) {
            if (data.addIslandFriend(islandID, friendID)) {
                source.sendMessage(Text.of(TextColors.GREEN,"Added " + friend.getName() + " as an island friend."));
            } else {
                source.sendMessage(Text.of(TextColors.DARK_RED,"Unable to add " + friend.getName() + " as an island friend."));
            }
        } else if(action.startsWith("rem")) {
            if (data.removeIslandFriend(islandID, friendID)) {
                source.sendMessage(Text.of(TextColors.GREEN,"Removed " + friend.getName() + " as an island friend."));
            } else {
                source.sendMessage(Text.of(TextColors.DARK_RED,"Unable to remove " + friend.getName() + " as an island friend."));
            }
        }
        return CommandResult.success();
    }
}
