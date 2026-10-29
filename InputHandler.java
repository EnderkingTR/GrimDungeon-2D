import java.awt.event.*;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {
    private final boolean[] keys = new boolean[256];
    public boolean up, down, left, right, debugToggle, regenerateMap, attack, skillUse, interact, saveGame, loadGame, escapeJustPressed, f11JustPressed;
    public boolean mouseAttack;
    public boolean rightMouseDown;
    public boolean mouseJustClicked;

    // Virtual coordinates (mapped to 960x640 game space)
    public int mouseX, mouseY;
    // Raw physical window coordinates
    private int rawMouseX, rawMouseY;
    private double viewScale = 1.0;
    private double viewOffsetX = 0.0;
    private double viewOffsetY = 0.0;

    private boolean debugPressedLastFrame = false;
    private boolean rPressedLastFrame = false;
    private boolean ePressedLastFrame = false;
    private boolean qPressedLastFrame = false;
    private boolean escPressedLastFrame = false;
    private boolean f11PressedLastFrame = false;
    private boolean f5PressedLastFrame = false;
    private boolean f9PressedLastFrame = false;
    private boolean leftMouseDownLastFrame = false;
    private boolean rightMouseDownLastFrame = false;

    public void setViewportTransform(double scale, double offsetX, double offsetY) {
        this.viewScale = scale <= 0 ? 1.0 : scale;
        this.viewOffsetX = offsetX;
        this.viewOffsetY = offsetY;
        recomputeVirtualMouse();
    }

    private void recomputeVirtualMouse() {
        this.mouseX = (int) Math.round((rawMouseX - viewOffsetX) / viewScale);
        this.mouseY = (int) Math.round((rawMouseY - viewOffsetY) / viewScale);
    }

    public void update() {
        recomputeVirtualMouse();

        up = keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP];
        down = keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN];
        left = keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT];
        right = keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT];
        attack = keys[KeyEvent.VK_SPACE];

        boolean qCurrent = keys[KeyEvent.VK_Q] || keys[KeyEvent.VK_SHIFT];
        boolean rightClickJust = rightMouseDown && !rightMouseDownLastFrame;
        skillUse = (qCurrent && !qPressedLastFrame) || rightClickJust;
        qPressedLastFrame = qCurrent;
        rightMouseDownLastFrame = rightMouseDown;

        mouseJustClicked = mouseAttack && !leftMouseDownLastFrame;
        leftMouseDownLastFrame = mouseAttack;

        boolean escCurrent = keys[KeyEvent.VK_ESCAPE];
        escapeJustPressed = escCurrent && !escPressedLastFrame;
        escPressedLastFrame = escCurrent;

        boolean f11Current = keys[KeyEvent.VK_F11];
        f11JustPressed = f11Current && !f11PressedLastFrame;
        f11PressedLastFrame = f11Current;

        boolean f3Current = keys[KeyEvent.VK_F3];
        if (f3Current && !debugPressedLastFrame)
            debugToggle = !debugToggle;
        debugPressedLastFrame = f3Current;

        boolean rCurrent = keys[KeyEvent.VK_R];
        regenerateMap = rCurrent && !rPressedLastFrame;
        rPressedLastFrame = rCurrent;

        boolean eCurrent = keys[KeyEvent.VK_E];
        interact = eCurrent && !ePressedLastFrame;
        ePressedLastFrame = eCurrent;

        boolean f5Current = keys[KeyEvent.VK_F5];
        saveGame = f5Current && !f5PressedLastFrame;
        f5PressedLastFrame = f5Current;

        boolean f9Current = keys[KeyEvent.VK_F9];
        loadGame = f9Current && !f9PressedLastFrame;
        f9PressedLastFrame = f9Current;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length)
            keys[code] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length)
            keys[code] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        rawMouseX = e.getX();
        rawMouseY = e.getY();
        recomputeVirtualMouse();
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseAttack = true;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            rightMouseDown = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        rawMouseX = e.getX();
        rawMouseY = e.getY();
        recomputeVirtualMouse();
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseAttack = false;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            rightMouseDown = false;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        rawMouseX = e.getX();
        rawMouseY = e.getY();
        recomputeVirtualMouse();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        rawMouseX = e.getX();
        rawMouseY = e.getY();
        recomputeVirtualMouse();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
