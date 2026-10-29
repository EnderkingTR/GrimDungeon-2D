import java.awt.event.*;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {
    private final boolean[] keys = new boolean[256];
    public boolean up, down, left, right, debugToggle, regenerateMap, attack, skillUse, interact, saveGame, loadGame, escapeJustPressed;
    public boolean mouseAttack;
    public boolean rightMouseDown;
    public boolean mouseJustClicked;
    public int mouseX, mouseY;

    private boolean debugPressedLastFrame = false;
    private boolean rPressedLastFrame = false;
    private boolean ePressedLastFrame = false;
    private boolean qPressedLastFrame = false;
    private boolean escPressedLastFrame = false;
    private boolean f5PressedLastFrame = false;
    private boolean f9PressedLastFrame = false;
    private boolean leftMouseDownLastFrame = false;
    private boolean rightMouseDownLastFrame = false;

    public void update() {
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
        mouseX = e.getX();
        mouseY = e.getY();
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseAttack = true;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            rightMouseDown = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseAttack = false;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            rightMouseDown = false;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
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
