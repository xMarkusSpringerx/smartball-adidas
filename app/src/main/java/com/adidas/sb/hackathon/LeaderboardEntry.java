package com.adidas.sb.hackathon;

public class LeaderboardEntry {

    private String playerName;
    private int juggles;

    public LeaderboardEntry(String playerName, int juggles) {
        this.playerName = playerName;
        this.juggles = juggles;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getJuggles() {
        return juggles;
    }

}
