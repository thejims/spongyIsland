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
        if(!args.hasAny("Friend")) {
            source.sendMessage(Text.of(new Object[]{TextColors.RED, "Invalid arguments"}));
            return CommandResult.success();
        } else {
            User user = args.<User>getOne("Friend").get();
            UUID target = user.getUniqueId();
            IslandPlayer isPlayer = data.getPlayerData(((Player) source).getUniqueId());
            if (isPlayer.getUUID() == target) return CommandResult.success();
           if (isPlayer.isFriend(target)) {
               data.removeFriend(((Player) source).getUniqueId(), target);
               source.sendMessage(Text.of(user.getName() +" Has Been Removed from your friends list"));

           }
           else
           {
               data.addFriend(((Player) source).getUniqueId(), target);
               source.sendMessage(Text.of(user.getName() +" Has Been Added from your friends list"));
           }
        }
        return CommandResult.success();
    }
}
