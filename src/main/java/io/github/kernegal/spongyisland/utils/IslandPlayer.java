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

package io.github.kernegal.spongyisland.utils;

import javax.annotation.Nullable;
import java.util.UUID;


public class IslandPlayer {
    private UUID uuid;
    private String name;
    private UUID island;
    private long newIslandTime, newLevelTime;

    public IslandPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUUID() {
        return uuid;
    }
    public String getName() {
        return name;
    }

    public void setIsland(UUID island) { this.island = island; }
    @Nullable public UUID getIsland() {
        return island;
    }

    // Not sure about these next two functions, whether the data is persistent or what have you..
    public void setNewIslandTime() { this.newIslandTime = System.currentTimeMillis(); }
    public void setNewLevelTime(){ this.newLevelTime = System.currentTimeMillis(); }

    public boolean canHaveNewIsland(int waitTime){
        return newIslandTime==0 || (System.currentTimeMillis()-newIslandTime)/1000>waitTime;
    }

    public boolean canCalculateIslandLevel(int waitTime){
        return newLevelTime==0 || (System.currentTimeMillis()-newLevelTime)/1000>waitTime;
    }
}
