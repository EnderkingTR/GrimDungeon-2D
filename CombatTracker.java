public class CombatTracker {
    public int totalDamageDealt = 0;
    public int totalDamageTaken = 0;
    public int enemiesKilled = 0;
    public int criticalHits = 0;
    public int goldEarnedInStage = 0;
    public int elapsedTicks = 0;

    public void reset() {
        totalDamageDealt = 0;
        totalDamageTaken = 0;
        enemiesKilled = 0;
        criticalHits = 0;
        goldEarnedInStage = 0;
        elapsedTicks = 0;
    }

    public String getFormattedTime() {
        int totalSeconds = elapsedTicks / 60;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}

