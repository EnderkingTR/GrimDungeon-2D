public enum GameState {
    MAIN_MENU,
    PLAYING,
    STAGE_CLEAR,
    GAME_OVER,
    CHEST_OPENING;

    public enum MenuTab {
        CAMPAIGN("SEFER"),
        HEROES("KARAKTER"),
        EQUIPMENT("EKİPMAN"),
        SKILLS("YETENEK"),
        SHOP("MARKET"),
        SETTINGS("AYARLAR");

        public final String title;

        MenuTab(String title) {
            this.title = title;
        }
    }

    public enum DisplayMode {
        WINDOWED("Pencereli (Windowed)"),
        BORDERLESS_FULLSCREEN("Pencereli Tam Ekran (Borderless)"),
        FULLSCREEN("Tam Ekran (Fullscreen)");

        public final String displayName;

        DisplayMode(String displayName) {
            this.displayName = displayName;
        }
    }
}
