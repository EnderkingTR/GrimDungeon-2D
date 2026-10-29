import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.*;

/**
 * Hybrid Audio Subsystem for GrimDungeon 2D.
 * 1) Automatically loads external .wav files from "assets/audio/sfx/<name>.wav" or "assets/audio/music/bgm.wav" if present.
 * 2) Falls back to an internal 16-bit PCM Procedural Synthesizer for all SFX and dark-fantasy ambient BGM
 *    so the game has full sound effects and music immediately with zero external dependencies.
 */
public class AudioManager {
    private static final float SAMPLE_RATE = 22050f;

    public enum Sfx {
        SWING("swing.wav"),
        SHOOT("shoot.wav"),
        HIT("hit.wav"),
        CRIT("crit.wav"),
        PLAYER_HURT("hurt.wav"),
        KILL("kill.wav"),
        COIN("coin.wav"),
        SKILL("skill.wav"),
        CHEST_OPEN("chest.wav"),
        STAGE_CLEAR("victory.wav"),
        GAME_OVER("gameover.wav"),
        UI_CLICK("click.wav");

        public final String fileName;

        Sfx(String fileName) {
            this.fileName = fileName;
        }
    }

    private static final Map<Sfx, byte[]> sfxPcmCache = new EnumMap<>(Sfx.class);
    private static final ExecutorService soundPool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "Audio-SFX-Worker");
        t.setDaemon(true);
        return t;
    });

    private static Clip musicClip;
    private static boolean sfxEnabled = true;
    private static boolean musicEnabled = true;
    private static float sfxVolume = 0.80f;   // 0.0 to 1.0
    private static float musicVolume = 0.55f; // 0.0 to 1.0
    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        // Ensure asset directories exist so the user knows where to drop custom files
        new File("assets/audio/sfx").mkdirs();
        new File("assets/audio/music").mkdirs();
        new File("assets/sprites").mkdirs();

        // Pre-synthesize fallback PCM buffers for instant zero-latency playback
        for (Sfx sfx : Sfx.values()) {
            sfxPcmCache.put(sfx, synthesizeSfx(sfx));
        }

        startBackgroundMusic();
    }

    public static boolean isSfxEnabled() {
        return sfxEnabled;
    }

    public static void setSfxEnabled(boolean enabled) {
        sfxEnabled = enabled;
    }

    public static boolean isMusicEnabled() {
        return musicEnabled;
    }

    public static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (musicEnabled) {
            startBackgroundMusic();
        } else {
            stopBackgroundMusic();
        }
    }

    public static float getSfxVolume() {
        return sfxVolume;
    }

    public static void setSfxVolume(float vol) {
        sfxVolume = Math.max(0f, Math.min(1f, vol));
    }

    public static float getMusicVolume() {
        return musicVolume;
    }

    public static void setMusicVolume(float vol) {
        musicVolume = Math.max(0f, Math.min(1f, vol));
        applyVolumeToClip(musicClip, musicVolume);
    }

    public static void play(Sfx sfx) {
        if (!initialized) init();
        if (!sfxEnabled || sfxVolume <= 0.01f) return;

        soundPool.submit(() -> {
            try {
                File customFile = new File("assets/audio/sfx/" + sfx.fileName);
                Clip clip = AudioSystem.getClip();
                if (customFile.exists()) {
                    try (AudioInputStream ais = AudioSystem.getAudioInputStream(customFile)) {
                        clip.open(ais);
                    }
                } else {
                    byte[] pcm = sfxPcmCache.get(sfx);
                    if (pcm == null) return;
                    AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
                    try (AudioInputStream ais = new AudioInputStream(
                            new ByteArrayInputStream(pcm), format, pcm.length / 2)) {
                        clip.open(ais);
                    }
                }
                applyVolumeToClip(clip, sfxVolume);
                clip.addLineListener(ev -> {
                    if (ev.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.start();
            } catch (Exception ignored) {
            }
        });
    }

    public static synchronized void startBackgroundMusic() {
        if (!musicEnabled) return;
        try {
            if (musicClip != null && musicClip.isRunning()) {
                return;
            }
            if (musicClip != null) {
                musicClip.close();
            }

            musicClip = AudioSystem.getClip();
            File customBgm = new File("assets/audio/music/bgm.wav");
            if (customBgm.exists()) {
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(customBgm)) {
                    musicClip.open(ais);
                }
            } else {
                byte[] loopPcm = synthesizeDungeonAmbientLoop();
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
                try (AudioInputStream ais = new AudioInputStream(
                        new ByteArrayInputStream(loopPcm), format, loopPcm.length / 2)) {
                    musicClip.open(ais);
                }
            }
            applyVolumeToClip(musicClip, musicVolume);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception ignored) {
        }
    }

    public static synchronized void stopBackgroundMusic() {
        try {
            if (musicClip != null) {
                musicClip.stop();
                musicClip.close();
                musicClip = null;
            }
        } catch (Exception ignored) {
        }
    }

    private static void applyVolumeToClip(Clip clip, float linearVolume) {
        if (clip == null) return;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (20.0 * Math.log10(Math.max(0.0001, linearVolume)));
                dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
                gain.setValue(dB);
            }
        } catch (Exception ignored) {
        }
    }

    private static byte[] synthesizeSfx(Sfx type) {
        Random rng = new Random(type.ordinal() * 997L);
        return switch (type) {
            case UI_CLICK -> generateTone(0.045, 780, 1180, 0.25, false, rng);
            case SWING -> generateNoiseSweep(0.11, 420, 140, 0.35, rng);
            case SHOOT -> generateTone(0.10, 660, 240, 0.32, false, rng);
            case HIT -> generateNoiseSweep(0.08, 220, 80, 0.45, rng);
            case CRIT -> generateChordArpeggio(new double[]{523.25, 659.25, 1046.5}, 0.045, 0.45);
            case PLAYER_HURT -> generateTone(0.18, 190, 75, 0.50, true, rng);
            case KILL -> generateTone(0.13, 310, 580, 0.38, false, rng);
            case COIN -> generateChordArpeggio(new double[]{987.77, 1318.51}, 0.065, 0.36);
            case SKILL -> generateTone(0.24, 240, 880, 0.42, true, rng);
            case CHEST_OPEN -> generateChordArpeggio(new double[]{440.0, 554.37, 659.25, 880.0}, 0.09, 0.42);
            case STAGE_CLEAR -> generateChordArpeggio(new double[]{523.25, 659.25, 783.99, 1046.50}, 0.11, 0.45);
            case GAME_OVER -> generateChordArpeggio(new double[]{392.0, 349.23, 311.13, 261.63}, 0.14, 0.42);
        };
    }

    private static byte[] generateTone(double durationSec, double startFreq, double endFreq,
                                       double amp, boolean addHarmonic, Random rng) {
        int samples = (int) (SAMPLE_RATE * durationSec);
        byte[] pcm = new byte[samples * 2];
        double phase = 0;
        for (int i = 0; i < samples; i++) {
            double progress = i / (double) samples;
            double env = Math.sin(progress * Math.PI); // Smooth attack & decay envelope
            double freq = startFreq + (endFreq - startFreq) * progress;
            phase += (2.0 * Math.PI * freq) / SAMPLE_RATE;

            double wave = Math.sin(phase);
            if (addHarmonic) {
                wave = 0.65 * wave + 0.35 * (Math.sin(phase * 2.0) >= 0 ? 0.8 : -0.8);
            }
            short val = (short) (wave * env * amp * Short.MAX_VALUE);
            pcm[i * 2] = (byte) (val & 0xFF);
            pcm[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
        }
        return pcm;
    }

    private static byte[] generateNoiseSweep(double durationSec, double baseFreq, double endFreq,
                                             double amp, Random rng) {
        int samples = (int) (SAMPLE_RATE * durationSec);
        byte[] pcm = new byte[samples * 2];
        double phase = 0;
        for (int i = 0; i < samples; i++) {
            double progress = i / (double) samples;
            double env = Math.pow(1.0 - progress, 1.4);
            double freq = baseFreq + (endFreq - baseFreq) * progress;
            phase += (2.0 * Math.PI * freq) / SAMPLE_RATE;

            double noise = (rng.nextDouble() * 2.0 - 1.0) * 0.55;
            double tone = Math.sin(phase) * 0.45;
            short val = (short) ((noise + tone) * env * amp * Short.MAX_VALUE);
            pcm[i * 2] = (byte) (val & 0xFF);
            pcm[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
        }
        return pcm;
    }

    private static byte[] generateChordArpeggio(double[] notes, double noteDurationSec, double amp) {
        int samplesPerNote = (int) (SAMPLE_RATE * noteDurationSec);
        int totalSamples = samplesPerNote * notes.length;
        byte[] pcm = new byte[totalSamples * 2];

        for (int n = 0; n < notes.length; n++) {
            double freq = notes[n];
            double phase = 0;
            for (int i = 0; i < samplesPerNote; i++) {
                int idx = n * samplesPerNote + i;
                double progress = i / (double) samplesPerNote;
                double env = Math.pow(1.0 - progress * 0.75, 1.2);
                phase += (2.0 * Math.PI * freq) / SAMPLE_RATE;
                double wave = 0.75 * Math.sin(phase) + 0.25 * Math.sin(phase * 2.0);
                short val = (short) (wave * env * amp * Short.MAX_VALUE);
                pcm[idx * 2] = (byte) (val & 0xFF);
                pcm[idx * 2 + 1] = (byte) ((val >> 8) & 0xFF);
            }
        }
        return pcm;
    }

    /**
     * Synthesizes a 6.4-second atmospheric dark-fantasy minor arpeggio loop (D minor / A minor progression)
     * so the dungeon and lobby have eerie retro synth background music out-of-the-box.
     */
    private static byte[] synthesizeDungeonAmbientLoop() {
        double[] sequence = {
            146.83, 220.00, 261.63, 220.00, // Dm: D3, A3, C4, A3
            130.81, 196.00, 246.94, 196.00, // C/G: C3, G3, B3, G3
            110.00, 164.81, 220.00, 164.81, // Am: A2, E3, A3, E3
            116.54, 174.61, 220.00, 164.81  // Bb -> A: Bb2, F3, A3, E3
        };
        double stepSec = 0.40;
        int stepSamples = (int) (SAMPLE_RATE * stepSec);
        int totalSamples = stepSamples * sequence.length;
        byte[] pcm = new byte[totalSamples * 2];

        double bassPhase = 0;
        double padPhase = 0;
        for (int s = 0; s < sequence.length; s++) {
            double noteFreq = sequence[s];
            double droneFreq = sequence[(s / 4) * 4] * 0.5; // Sub-bass root
            for (int i = 0; i < stepSamples; i++) {
                int idx = s * stepSamples + i;
                double p = i / (double) stepSamples;
                double env = Math.sin(p * Math.PI);

                bassPhase += (2.0 * Math.PI * noteFreq) / SAMPLE_RATE;
                padPhase += (2.0 * Math.PI * droneFreq) / SAMPLE_RATE;

                double pluck = Math.sin(bassPhase) * env * 0.16;
                double drone = (Math.sin(padPhase) * 0.7 + Math.sin(padPhase * 1.5) * 0.3) * 0.10;
                short val = (short) ((pluck + drone) * Short.MAX_VALUE);
                pcm[idx * 2] = (byte) (val & 0xFF);
                pcm[idx * 2 + 1] = (byte) ((val >> 8) & 0xFF);
            }
        }
        return pcm;
    }
}

