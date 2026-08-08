package ro.f01.id6launcher;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class IdriveAccessibilityService extends AccessibilityService {

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        MainActivity.lastGlobalKey =
                "GLOBAL " +
                (event.getAction()==KeyEvent.ACTION_DOWN ? "DOWN" : "UP") +
                " code=" + event.getKeyCode() +
                " " + KeyEvent.keyCodeToString(event.getKeyCode()) +
                " scan=" + event.getScanCode() +
                " repeat=" + event.getRepeatCount();

        // Diagnostic only: do not consume keys.
        return false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}
}
