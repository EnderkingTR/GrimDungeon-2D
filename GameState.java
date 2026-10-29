public enum GameState {
    MAIN_MENU,
    PLAYING,
    STAGE_CLEAR,
    GAME_OVER,
    CHEST_OPENING;

    public enum MenuTab {
        CAMPAIGN("SEFER HARİTASI"),
        HEROES("KARAKTERLER"),
        EQUIPMENT("EKİPMANLAR"),
        SKILLS("YETENEKLER"),
        SHOP("MARKET & SANDIK");

        public final String title;

        MenuTab(String title) {
            this.title = title;
        }
    }
}

