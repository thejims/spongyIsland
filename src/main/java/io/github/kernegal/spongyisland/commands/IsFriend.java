package io.github.kernegal.spongyisland.commands;

import io.github.kernegal.spongyisland.DataHolder;
import io.github.kernegal.spongyisland.utils.IslandManager;
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

/**
 * Created by Bebabo10 on 1/1/2017.
 */
public class IsFriend implements CommandExecutor {
    @Override
    @Nonnull
    public CommandResult execute(@Nonnull CommandSource source, @Nonnull CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Text.of(TextColors.RED, "Player only."));
            return CommandResult.success();
        }
        if(!args.hasAny("target")) {
            source.sendMessage(Text.of(new Object[]{TextColors.RED, "Invalid arguments"}));
            return CommandResult.success();
        } else {
            User user = args.<User>getOne("Friend").get();
            String target = user.getName();

           if (IslandPlayer.ifFriend(target))
           {
               IslandPlayer.renFriend(target);
                source.sendMessage(Text.of(target+" Has Been Removed from your friends list"));

           }
           else
           {
               IslandPlayer.addFriend(target);
               source.sendMessage(Text.of(target+" Has Been Added from your friends list"));
           }
        }
        return CommandResult.success();
    }
}
