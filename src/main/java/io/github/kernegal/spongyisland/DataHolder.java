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

package io.github.kernegal.spongyisland;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import io.github.kernegal.spongyisland.utils.CompletedChallenges;
import io.github.kernegal.spongyisland.utils.IslandPlayer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

//TODO: Separate into PlayerStore, IslandStore, and ChallengeStore classes
public class DataHolder {

    private Map<UUID, IslandPlayer> players;
    private Map<UUID, CompletedChallenges> playerChallenges;
    private ConfigurationNode challenges, config;
    private Random rg;
    private ConfigurationLoader<CommentedConfigurationNode> playerStore, islandStore, challengeStore;
    private ConfigurationNode playerStoreNode, islandStoreNode, challengeStoreNode;

    public DataHolder(ConfigurationNode challenges, ConfigurationNode config,
                      ConfigurationLoader<CommentedConfigurationNode> playerStore,
                      ConfigurationLoader<CommentedConfigurationNode> islandStore,
                      ConfigurationLoader<CommentedConfigurationNode> challengeStore) {
        this.playerStore = playerStore;
        this.islandStore = islandStore;
        this.challengeStore = challengeStore;
        try {
            this.playerStoreNode = this.playerStore.load();
            this.islandStoreNode = this.islandStore.load();
            this.challengeStoreNode = this.challengeStore.load();
        } catch (IOException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
        }

        this.challenges = challenges;
        this.config = config;
        players = new HashMap<>();
        playerChallenges = new HashMap<>();
        rg= new Random();
    }

    private void saveIslandStore() {
        // TODO: Async this function
        try {
            islandStore.save(islandStoreNode);
        } catch (IOException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
        }
    }
    private void saveChallengeStore() {
        // TODO: Async this function
        try {
            challengeStore.save(challengeStoreNode);
        } catch (IOException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
        }
    }
    private void savePlayerStore() {
        // TODO: Async this function
        try {
            playerStore.save(playerStoreNode);
        } catch (IOException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
        }
    }

    public void playerLogin(Player p) {
        UUID uuid = p.getUniqueId();
        loadPlayer(uuid);
        loadPlayerChallenges(uuid);
    }

    private void loadPlayer(UUID uuid) {
        // TODO: Async this function
        if (players.containsKey(uuid)) // Already loaded
            return;

        // TODO: Use sponge userstorage instead in case player drops
        String playerName = Sponge.getServer().getPlayer(uuid).get().getName();
        ConfigurationNode playerNode = playerStoreNode.getNode(uuid.toString());
        IslandPlayer isPlayer = new IslandPlayer(uuid, playerName);

        try {
            // Just incase it's the first time the player has logged in or they.. changed their name!!
            if (playerNode.getNode("name").isVirtual() || !playerNode.getNode("name").getString().equals(playerName)) {
                playerNode.getNode("name").setValue(playerName);
                savePlayerStore();
            }

            if (!playerNode.getNode("island").isVirtual())
                isPlayer.setIsland(playerNode.getNode("island").getValue(TypeToken.of(UUID.class)));

            players.put(uuid, isPlayer);

        } catch (ObjectMappingException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
        }
    }
    private void setPlayerIsland(UUID player, UUID island) {
        players.get(player).setIsland(island);
        playerStoreNode.getNode(player.toString()).getNode("island").setValue(island);
        savePlayerStore();
    }
    public final IslandPlayer getPlayerData(UUID uuid){
        if (!players.containsKey(uuid))
            loadPlayer(uuid);
        return players.get(uuid);
    }

    private void loadPlayerChallenges(UUID uuid) {
        // TODO: Async this function
        if (playerChallenges.containsKey(uuid)) // Already loaded
            return;

        ConfigurationNode challengeNode = challengeStoreNode.getNode(uuid.toString());
        CompletedChallenges c = new CompletedChallenges(challenges.getNode("root_level").getString());
        playerChallenges.put(uuid,c);
        challengeNode.getChildrenList().forEach(challenge -> {
            c.setChallengeCompleted(challenge.getString(),
                    challenges.getNode("challenge_list",challenge.getString(),"level").getString(),
                    challenge.getNode("ntimes").getInt());
        });
    }

    private void unloadPlayerChallenges(UUID uuid) {
        if (!playerChallenges.containsKey(uuid))
            return;
        playerChallenges.remove(uuid);
    }

    public IslandPlayer nearestIsland(Vector2i location) {
        //TODO: Fix this
        String nearestIslandId = "";
        Float nearestIslandDistance = null;
        try {
            for (ConfigurationNode island:islandStoreNode.getChildrenList()) {
                Vector2i islandPos = island.getNode("location").getValue(TypeToken.of(Vector3i.class)).toVector2();
                Float newIslandDistance = location.distance(islandPos);
                if (nearestIslandDistance == null || newIslandDistance < nearestIslandDistance) {
                    nearestIslandDistance = newIslandDistance;
                    nearestIslandId = island.getString();
                }
            }
        } catch (ObjectMappingException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
        }

        return players.get(UUID.fromString(nearestIslandId));
    }

    public Vector2i[] getLastTwoIslandsPosition(){
        Vector2i[] res = new Vector2i[2];
        try {
            res[0] = islandStoreNode.getNode("last_island_position").getValue(TypeToken.of(Vector2i.class), (Vector2i)null);
            res[1] = islandStoreNode.getNode("pre_last_island_position").getValue(TypeToken.of(Vector2i.class), (Vector2i)null);
        } catch(ObjectMappingException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
        }
        return res;
    }

    public void newIsland(Vector2i position, Vector3i worldPos, Player player){
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        ConfigurationNode islandNode = islandStoreNode.getNode(uuid);
        ConfigurationNode playerNode = playerStoreNode.getNode(uuid);
        try {
            playerNode.getNode("island").setValue(TypeToken.of(UUID.class), uuid);
            islandNode.getNode("location").setValue(TypeToken.of(Vector3i.class), worldPos);
            islandNode.getNode("position").setValue(TypeToken.of(Vector2i.class), position);
            islandNode.getNode("name").setValue(name);
            islandNode.getNode("home").setValue(TypeToken.of(Vector3i.class), worldPos);
            islandNode.getNode("friends").setValue(Arrays.asList(uuid));
            if (!islandStoreNode.getNode("last_island_position").isVirtual())
                islandStoreNode.getNode("pre_last_island_position").setValue(TypeToken.of(Vector2i.class), islandStoreNode.getNode("last_island_position").getValue(TypeToken.of(Vector2i.class)));
            islandStoreNode.getNode("last_island_position").setValue(TypeToken.of(Vector2i.class), position);
        } catch (ObjectMappingException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
        }
        saveIslandStore();
        savePlayerStore();
    }

    public Vector3i getIslandHome(UUID island) {
        ConfigurationNode islandNode = islandStoreNode.getNode(island);
        try {
            return islandNode.getNode("home").getValue(TypeToken.of(Vector3i.class));
        } catch (ObjectMappingException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
            return null;
        }
    }
    public void setIslandHome(UUID island, Vector3i pos){
        ConfigurationNode islandNode = islandStoreNode.getNode(island);
        islandNode.getNode("home").setValue(pos);
        saveIslandStore();
    }
    public Vector3i getIslandLocation(UUID island) {
        ConfigurationNode islandNode = islandStoreNode.getNode(island);
        try {
            return islandNode.getNode("location").getValue(TypeToken.of(Vector3i.class));
        } catch (ObjectMappingException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
            return null;
        }
    }
    public String getIslandName(UUID island) {
        ConfigurationNode islandNode = islandStoreNode.getNode(island);
        return islandNode.getNode("name").getString();
    }
    public void setIslandName(UUID island, String name){
        ConfigurationNode islandNode = islandStoreNode.getNode(island);
        islandNode.getNode("name").setValue(name);
        saveIslandStore();
    }

    public boolean addIslandFriend(UUID island, UUID friend) {
        ConfigurationNode islandNode = islandStoreNode.getNode(island);
        try {
            List<UUID> friends = islandNode.getNode("friends").getList(TypeToken.of(UUID.class), new ArrayList<UUID>());
            if (!friends.contains(friend)) {
                friends.add(friend);
                islandNode.getNode("friends").setValue(friends);
                saveIslandStore();
            }
            return true;
        } catch (ObjectMappingException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
            return false;
        }
    }
    public boolean removeIslandFriend(UUID island, UUID friend) {
        ConfigurationNode islandNode = islandStoreNode.getNode(island);
        try {
            List<UUID> friends = islandNode.getNode("friends").getList(TypeToken.of(UUID.class), new ArrayList<UUID>());
            if (friends.contains(friend)) {
                friends.remove(friend);
                islandNode.getNode("friends").setValue(friends);
                saveIslandStore();
            }
            return true;
        } catch (ObjectMappingException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
            return false;
        }
    }

    public void setIslandLevel(UUID uuid, int level){
        islandStoreNode.getNode(uuid.toString()).getNode("level").setValue(level);
        saveIslandStore();
    }
    public void teleportPlayerToIsland(Player player, UUID island) {
        IslandPlayer playerData = getPlayerData(player.getUniqueId());
        ConfigurationNode islandNode = islandStoreNode.getNode(island);

        try {
            // Requested island does not exist
            if (islandNode.isVirtual()) {
                player.sendMessage(Text.of("The requested island could not be found"));
                return;
            }

            // The player is not a friend on the island
            if (!islandNode.getNode("friends").getList(TypeToken.of(UUID.class)).contains(playerData.getUUID())) {
                player.sendMessage(Text.of("You are not a friend on the requested island"));
                return;
            }

            Vector3i islandHome = islandNode.getNode("home").getValue(TypeToken.of(Vector3i.class));
            Location<World> worldLocation = Sponge.getServer().getWorld("world").get().getLocation(islandHome);

            SpongyIsland.getPlugin().getLogger().info("Teleporting player " + player.getName() + " to " + islandHome);
            Optional<Location<World>> safeLocation = Sponge.getGame().getTeleportHelper()
                    .getSafeLocation(worldLocation, 10, 20);

            player.sendMessage(Text.of("Teleporting to island"));
            if (safeLocation.isPresent())
                player.setLocation(safeLocation.get());
            else
                player.sendMessage(Text.of(TextColors.DARK_RED, "Island not secure"));
        } catch (ObjectMappingException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
        }
    }
    public void teleportPlayerToHome(Player player){
        IslandPlayer playerData = getPlayerData(player.getUniqueId());
        ConfigurationNode islandNode = islandStoreNode.getNode(playerData.getIsland());

        // Player doesn't have an island to call home :(
        if (islandNode.isVirtual()) {
            player.sendMessage(Text.of("You do not have an island"));
            return;
        }

        try {
            Vector3i playerHome = islandNode.getNode("home").getValue(TypeToken.of(Vector3i.class));
            Location<World> worldLocation = Sponge.getServer().getWorld("world").get().getLocation(playerHome);

            SpongyIsland.getPlugin().getLogger().info("Teleporting player " + player.getName() + " to " + playerHome);
            Optional<Location<World>> safeLocation = Sponge.getGame().getTeleportHelper()
                    .getSafeLocation(worldLocation, 10, 20);

            player.sendMessage(Text.of("Teleporting to your island"));
            if (safeLocation.isPresent())
                player.setLocation(safeLocation.get());
            else
                player.sendMessage(Text.of(TextColors.DARK_RED, "Island not secure"));
        } catch (ObjectMappingException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
        }
    }


    //TODO
    /*public void listTopIslands(int number, CommandSource destination){
        destination.sendMessage(Text.of("Top "+number+" islands:"));
        try(Connection conn = getDataSource().getConnection()) {
            try {
                ResultSet rs  = conn.prepareStatement(
                        "SELECT island.id,island.name as island_name,island.level,c.name as player_name,count(player.id) as num_players " +
                        "FROM island " +
                        "JOIN player ON player.island=island.id " +
                        "JOIN player AS c ON c.id=island.creator_id " +
                        "GROUP BY island.id " +
                        "ORDER BY level " +
                        "DESC LIMIT "+number+";"
                    ).executeQuery();
                while (rs.next()) {
                    String name = rs.getString("island_name");
                    if(name==name){

                        destination.sendMessage(Text.of("Island of "+rs.getString("player_name")+": ",TextColors.AQUA,rs.getInt("level"),TextColors.NONE," ["+rs.getInt("num_players")+" players]"));
                    }
                    else{
                        destination.sendMessage(Text.of(name+": "+rs.getInt("level")+"["+rs.getInt("num_players")+" players]"));
                    }
                }
            } finally {
                conn.close();
            }
        } catch (SQLException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
        }
    }*/

    /*
     * Used to unlink an island???
     */
    public void markIslandAsSpecial(UUID uuid){
        ConfigurationNode islandNode = islandStoreNode.getNode(uuid.toString());
        islandNode.getNode("special").setValue(true);
        try {
            List<UUID> players = islandNode.getNode("players").getList(TypeToken.of(UUID.class));
            players.forEach(player -> { playerStoreNode.getNode(player.toString()).removeChild("island"); });
        } catch (ObjectMappingException e) {
            SpongyIsland.getPlugin().getLogger().error(e.toString());
        }
        islandNode.removeChild("players");
        savePlayerStore();
        saveIslandStore();
    }

    public boolean hasAccessToLevel(UUID player, String level){
        String root=challenges.getNode("root_level").getString();
        String requiredLevel=challenges.getNode("level_list",level,"required_level").getString("");
        int challengesRequired = challenges.getNode("level_list",requiredLevel,"required_challenges").getInt(0);

        return level.equals(root) || requiredLevel.isEmpty()
                || playerChallenges.get(player).challengesCompletedInLevel(requiredLevel)>=challengesRequired;

    }

    public boolean challengeIsCompleted(UUID player, String challenge){
        return playerChallenges.get(player).timesCompleted(challenge)!=0;
    }

    public boolean canCompleteChallenge(UUID player, String challenge){
        ConfigurationNode challengeNode=challenges.getNode("challenge_list",challenge);
        int timesCompleted = playerChallenges.get(player).timesCompleted(challenge);
        int maxTimes = challengeNode.getNode("max_times").getInt(0);
        return timesCompleted !=0 &&
                (!challengeNode.getNode("repeatable").getBoolean() ||
                maxTimes!=0 && timesCompleted>=maxTimes);

    }

    public void completeChallenge(String challenge, int times, Player player){

        ConfigurationNode challengeNode=challenges.getNode("challenge_list",challenge);
        if(challengeNode.getNode("friendly_name").getString()==null){
            player.sendMessage(Text.of("That challenge don't exist"));
            return;
        }
        if(!hasAccessToLevel(player.getUniqueId(),challengeNode.getNode("level").getString(""))){
            player.sendMessage(Text.of("You don't have access to that level"));
            return;
        }

        if(canCompleteChallenge(player.getUniqueId(),challenge)){
            player.sendMessage(Text.of("You can't complete that challenge any more times"));
            return;
        }

        String type = challengeNode.getNode("type").getString("");
        String requiredItems = challengeNode.getNode("required_items").getString("");
        String itemReward;
        String textReward;
        int moneyReward,expReward;

        int timesCompleted = playerChallenges.get(player.getUniqueId()).timesCompleted(challenge);

        if(timesCompleted==0) {
            itemReward = challengeNode.getNode("item_reward").getString("");
            textReward = challengeNode.getNode("reward_text").getString("");
            expReward = challengeNode.getNode("exp_eward").getInt(0);
            moneyReward = challengeNode.getNode("money_reward").getInt(0);

        }
        else{
            itemReward = challengeNode.getNode("repeat_item_reward").getString("");
            textReward = challengeNode.getNode("repeat_reward_text").getString("");
            expReward = challengeNode.getNode("repeat_exp_reward").getInt(0);
            moneyReward = challengeNode.getNode("money_reward").getInt(0);

        }



        if(type.equals("inventory") ){
            String[] requiredItemsArray = requiredItems.split(" ");
            ArrayList< ArrayList<Slot> > inventorySlots = new ArrayList<>(requiredItemsArray.length);
            for(int i=0;i<requiredItemsArray.length;i++){
                String item= requiredItemsArray[i];
                inventorySlots.add(new ArrayList<>());
                String[] itemElements = item.split(",");
                long itemUnsafeDamage;
                int quantity;

                if(itemElements.length>2){
                    itemUnsafeDamage=Integer.parseInt(itemElements[1]);
                    quantity=Integer.parseInt(itemElements[2]);
                }
                else{
                    quantity=Integer.parseInt(itemElements[1]);
                    itemUnsafeDamage=-1;
                }
                Optional<ItemType> itemType = Sponge.getGame().getRegistry().getType(ItemType.class, itemElements[0]);
                if(itemType.isPresent()){
                    Iterable<Slot> slotIter = player.getInventory().slots();

                    int needed=quantity;
                    for (Slot slot: slotIter){
                        Optional<ItemStack> slotItemStack = slot.peek();
                        if(slotItemStack.isPresent()){
                            ItemStack itemStack= slotItemStack.get();
                            Long unsafeDamage = itemStack.toContainer().getLong(DataQuery.of("UnsafeDamage")).orElse(0L);

                            if(itemType.get().matches(itemStack) && !(itemUnsafeDamage!=-1 && itemUnsafeDamage!=unsafeDamage)){
                                inventorySlots.get(i).add(slot);
                                needed-=itemStack.getQuantity();
                                if(needed<=0) break;
                            }
                        }

                    }
                    if(needed>0){
                        player.sendMessage(Text.of("You don't have all required items"));
                        return;
                    }

                }
                else{
                    SpongyIsland.getPlugin().getLogger().warn("item type incorrect: "+itemElements[0]);
                }


            }
            if(challengeNode.getNode("take_items").getBoolean(true)) {
                for (int i = 0; i < requiredItemsArray.length; i++) {

                    String item = requiredItemsArray[i];
                    String[] split = item.split(",");
                    int quantity = split.length > 2 ?
                            Integer.parseInt(split[2]) :
                            Integer.parseInt(split[1]);

                    ArrayList<Slot> slotArray = inventorySlots.get(i);
                    for (Slot slot : slotArray) {
                        if (slot.getStackSize() < quantity) {
                            quantity -= slot.getStackSize();
                            slot.clear();
                        } else {
                            slot.poll(quantity);
                        }
                    }
                }
            }


        }
        else if( type.equals("level") ){
            ConfigurationNode islandNode = islandStoreNode.getNode(players.get(player.getUniqueId()).getIsland());
            if(islandNode.getNode("level").getInt(0) < challengeNode.getNode("required_items").getInt(0)){
                player.sendMessage(Text.of(TextColors.DARK_RED,"You don't have the required level"));
                return;
            }
        }
        else if( type.equals("island") ){
            IslandPlayer playerData = players.get(player.getUniqueId());
            ConfigurationNode islandNode = islandStoreNode.getNode(players.get(player.getUniqueId()).getIsland());

            int islandRadius = config.getNode("island","radius").getInt(), protectionRadius=config.getNode("island","protectionRadius").getInt();
            Vector2i islandCoordinates=null;
            try {
                islandCoordinates = islandNode.getNode("position").getValue(TypeToken.of(Vector2i.class)).mul(islandRadius*2);
            } catch (ObjectMappingException e) {
                SpongyIsland.getPlugin().getLogger().error(e.toString());
            }

            Location<World> location = player.getLocation();

            Vector2i min = islandCoordinates.sub(protectionRadius,protectionRadius);
            Vector2i max = islandCoordinates.add(protectionRadius,protectionRadius);

            if(!location.getExtent().getName().equals("world") ||
                    location.getX()<min.getX() || location.getX()>=max.getX() ||
                    location.getZ()<min.getY() || location.getZ()>=max.getY()){
                player.sendMessage(Text.of(TextColors.DARK_RED,"You need to be inside of your island"));
                return;
            }

            String[] requiredItemsArray = requiredItems.split(" ");
            for(int i=0;i<requiredItemsArray.length;i++) {

                String item= requiredItemsArray[i];
                String[] itemElements = item.split(",");
                int itemUnsafeDamage;
                int quantity;

                if(itemElements.length>2){
                    itemUnsafeDamage=Integer.parseInt(itemElements[1]);
                    quantity=Integer.parseInt(itemElements[2]);
                }
                else{
                    quantity=Integer.parseInt(itemElements[1]);
                    itemUnsafeDamage=0;
                }

                Optional<BlockType> blockType = Sponge.getGame().getRegistry().getType(BlockType.class, itemElements[0]);

                if(blockType.isPresent()) {
                    Vector3i playerPosition = player.getLocation().getBlockPosition();

                    World world = Sponge.getServer().getWorld("world").get();
                    Extent view = world.getExtentView(playerPosition.sub(10, 10, 10),
                            playerPosition.add(10, 10, 10));

                    GsonConfigurationLoader loader = GsonConfigurationLoader.builder().build();
                    ConfigurationNode node = loader.createEmptyNode();

                    node.getNode("ContentVersion").setValue(1);
                    node.getNode("ItemType").setValue(itemElements[0]);
                    node.getNode("Count").setValue(quantity);
                    node.getNode("UnsafeDamage").setValue(itemUnsafeDamage);


                    int sum = view.getBlockWorker(Cause.of(NamedCause.of("plugin", SpongyIsland.getPlugin().getPluginContainer()))).reduce(
                            (vol, x, y, z, red) -> vol.getBlockType(x, y, z).equals(BlockTypes.AIR) ?
                                    red :
                                    red + isSameBlockType(vol.getBlock(x, y, z),blockType.get(),itemUnsafeDamage)
                            ,
                            (a, b) -> a + b,
                            0);
                    if(sum<quantity){
                        player.sendMessage(Text.of(TextColors.DARK_RED,"All the required items needs to be 10 blocks nar you"));
                        return;
                    }

                }

            }
        }
        else{
            player.sendMessage(Text.of("Not implemented"));
            return;
        }


        player.sendMessage(Text.of(textReward));
        String[] itemsStr = itemReward.split(" ");
        giveItem(player,itemsStr);

        String randomReward = challengeNode.getNode("random_reward").getString();
        if(randomReward!=null) {
            String[] rr = randomReward.split(",");
            ConfigurationNode randomRewards = challenges.getNode("random_rewards", rr[0]);
            int randomRewardTimes = 1;
            if(rr.length>1){
                randomRewardTimes=Integer.parseInt(rr[1]);
            }
            for(int i=0;i<randomRewardTimes;i++) {
                double cumulative = 0;
                double random = rg.nextDouble();

                for (Map.Entry<Object, ? extends ConfigurationNode> entry : randomRewards.getChildrenMap().entrySet()) {
                    double prob = entry.getValue().getNode("probability").getDouble();
                    cumulative += prob;
                    if (random < cumulative) {
                        giveItem(player, entry.getValue().getNode("reward").getString("").split(" "));
                        break;
                    }
                }
            }
        }


        if(config.getNode("general","economy").getBoolean(false)) {
            Optional<UniqueAccount> orCreateAccount = SpongyIsland.getPlugin().getEconomyService().getOrCreateAccount(player.getUniqueId());
            if(orCreateAccount.isPresent()){
                orCreateAccount.get().deposit(
                        SpongyIsland.getPlugin().getEconomyService().getDefaultCurrency(),
                        BigDecimal.valueOf((long)moneyReward),
                        Cause.source(this).build()
                );

            }
        }

        player.offer(Keys.TOTAL_EXPERIENCE,player.get(Keys.TOTAL_EXPERIENCE).orElse(0)+expReward);

        ConfigurationNode playerChallengeNode = challengeStoreNode.getNode(player.getUniqueId().toString());
        timesCompleted++;
        playerChallengeNode.getNode(challenge).getNode("ntimes").setValue(timesCompleted);
        saveChallengeStore();
        playerChallenges.get(player.getUniqueId()).setChallengeCompleted(challenge,challengeNode.getNode("level").getString(""));
    }

    private void giveItem(Player player, String[] itemsStr){
        for(String itemStr :itemsStr){
            String[] itemElements = itemStr.split(",");
            Optional<ItemType> itemType = Sponge.getGame().getRegistry().getType(ItemType.class, itemElements[0]);
            if(itemType.isPresent()) {
                int itemUnsafeDamage;
                int quantity;

                if (itemElements.length > 2) {
                    itemUnsafeDamage = Integer.parseInt(itemElements[1]);
                    quantity = Integer.parseInt(itemElements[2]);
                } else {
                    quantity = Integer.parseInt(itemElements[1]);
                    itemUnsafeDamage = 0;
                }

                //StringWriter sink = new StringWriter();
                //GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setSink(() -> new BufferedWriter(sink)).build();
                GsonConfigurationLoader loader = GsonConfigurationLoader.builder().build();
                ConfigurationNode node = loader.createEmptyNode();

                node.getNode("ContentVersion").setValue(1);
                node.getNode("ItemType").setValue(itemElements[0]);
                node.getNode("Count").setValue(quantity);
                node.getNode("UnsafeDamage").setValue(itemUnsafeDamage);
                if(itemElements.length>3){
                    ConfigurationNode subNode = node.getNode("UnsafeData");
                    String[] data = itemElements[3].split("\\.");
                    for(int i=0;i<data.length-1;i++){
                        subNode=subNode.getNode(data[i]);
                    }
                    SpongyIsland.getPlugin().getLogger().info("data["+(data.length - 1)+"]"+data.length+" "+itemElements[3]);
                    String[] split = data[data.length - 1].split(":");
                    subNode.getNode(split[0]).setValue(split[1]);
                }

                /*try {
                    loader.save(node);
                    String json = sink.toString();
                    player.sendMessage(Text.of(json));
                }  catch (IOException e) {
                    e.printStackTrace();
                }*/

                ItemStack itemStack;
                try {
                    itemStack= node.getValue(TypeToken.of(ItemStack.class));
                } catch (ObjectMappingException e) {
                    SpongyIsland.getPlugin().getLogger().warn(e.toString());
                    return;
                }


                InventoryTransactionResult transactionResult = player.getInventory().offer(itemStack);
                for (ItemStackSnapshot itemStackSnapshot : transactionResult.getRejectedItems() ){
                    Entity entity = player.getLocation().getExtent().createEntity(EntityTypes.ITEM, player.getLocation().getPosition());
                    entity.offer(Keys.REPRESENTED_ITEM, itemStackSnapshot);
                    player.getLocation().getExtent().spawnEntity(entity, Cause.of(NamedCause.of("player",player)));
                }


            }
            else{
                SpongyIsland.getPlugin().getLogger().warn("item type incorrect: "+itemElements[0]);
            }

        }

    }

    private int isSameBlockType(BlockState bs, BlockType type, int damage){
        //ItemStack is=ItemStack.builder().fromBlockState(bs).build();
        if(bs.getType().equals(type)){
            /*if(damage!=0) {
                Optional<Long> unsafeDamage = is.toContainer().getLong(DataQuery.of("UnsafeDamage"));
                if(unsafeDamage.isPresent() && unsafeDamage.get()==damage){
                    return 1;
                }

            }
            else{
                return 1;
            }*/
            return 1;
        }
        return 0;
    }

    public String getLastLevelIssued(UUID uuid){
        return playerChallenges.get(uuid).getLastLevel();
    }

    public int getCompletedChallengesInLevel(UUID uuid,String level){
        return playerChallenges.get(uuid).challengesCompletedInLevel(level);
    }

    public void resetChallenges(UUID uuid){
        challengeStoreNode.removeChild(uuid.toString());
        saveChallengeStore();
    }
}
