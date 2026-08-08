package ro.f01.id6launcher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.*;
import android.graphics.*;
import android.provider.Settings;
import android.view.*;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    DiagnosticView view;
    BroadcastReceiver receiver;

    public static String lastGlobalKey = "Niciun eveniment global";
    public static String lastBroadcast = "Niciun broadcast detectat";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideBars();

        view = new DiagnosticView(this);
        setContentView(view);

        registerProbeReceiver();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(receiver); } catch (Exception ignored) {}
    }

    void hideBars() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);
        if (f) hideBars();
    }

    void registerProbeReceiver() {
        receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Bundle extras = intent.getExtras();
                StringBuilder sb = new StringBuilder();
                sb.append(action == null ? "(fara action)" : action);

                if (extras != null) {
                    for (String key : extras.keySet()) {
                        Object val = extras.get(key);
                        sb.append(" | ").append(key).append("=")
                          .append(String.valueOf(val));
                    }
                }

                lastBroadcast = sb.toString();
                if (view != null) view.invalidate();
            }
        };

        IntentFilter f = new IntentFilter();

        // Android / media / common car-HU actions
        f.addAction(Intent.ACTION_MEDIA_BUTTON);
        f.addAction(Intent.ACTION_HEADSET_PLUG);
        f.addAction(Intent.ACTION_SCREEN_ON);
        f.addAction(Intent.ACTION_SCREEN_OFF);

        // Common aftermarket HU patterns (probe only)
        f.addAction("com.microntek.irkeyDown");
        f.addAction("com.microntek.irkeyUp");
        f.addAction("com.microntek.canbuschange");
        f.addAction("com.ts.main.BROADCAST");
        f.addAction("com.ts.can.BROADCAST");
        f.addAction("com.ts.canbus.BROADCAST");
        f.addAction("com.ts.bt.BROADCAST");
        f.addAction("com.ts.main.KEY");
        f.addAction("com.ts.main.EVENT");
        f.addAction("com.yx.can.BROADCAST");
        f.addAction("com.yx.bmw.BROADCAST");

        try {
            registerReceiver(receiver, f);
        } catch (Exception e) {
            lastBroadcast = "registerReceiver: " + e.toString();
        }
    }

    @Override public boolean dispatchKeyEvent(KeyEvent e) {
        if (view != null) {
            view.lastActivityKey =
                    "ACTIVITY " +
                    (e.getAction()==KeyEvent.ACTION_DOWN ? "DOWN" : "UP") +
                    " code=" + e.getKeyCode() +
                    " " + KeyEvent.keyCodeToString(e.getKeyCode()) +
                    " scan=" + e.getScanCode() +
                    " repeat=" + e.getRepeatCount();
            view.invalidate();
        }
        return super.dispatchKeyEvent(e);
    }

    class DiagnosticView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Handler handler = new Handler();

        String lastActivityKey = "Astept KeyEvent in Activity...";
        String lastMotion = "Niciun MotionEvent detectat";

        final float BW=1280f, BH=480f;

        Runnable tick = new Runnable() {
            @Override public void run() {
                invalidate();
                handler.postDelayed(this, 500);
            }
        };

        DiagnosticView(Context c) {
            super(c);
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
            handler.post(tick);
        }

        float sx(){ return getWidth()/BW; }
        float sy(){ return getHeight()/BH; }

        void fill(Canvas c,int color,float l,float t,float r,float b){
            p.setColor(color); p.setStyle(Paint.Style.FILL);
            c.drawRect(l*sx(),t*sy(),r*sx(),b*sy(),p);
        }

        void text(Canvas c,String s,float x,float y,float size,int color){
            p.setColor(color); p.setStyle(Paint.Style.FILL);
            p.setTextAlign(Paint.Align.LEFT);
            p.setTypeface(Typeface.create("sans",Typeface.NORMAL));
            p.setTextSize(size*sy());
            c.drawText(s,x*sx(),y*sy(),p);
        }

        @Override protected void onDraw(Canvas c) {
            c.drawColor(Color.rgb(5,7,9));

            fill(c,Color.rgb(22,25,28),0,0,1280,54);

            String tm = new SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(new Date());
            text(c,"iDrive V5 DIAGNOSTIC",24,35,20,Color.WHITE);
            text(c,tm,1140,35,16,Color.LTGRAY);

            text(c,"1. Roteste  2. Apasa OK  3. BACK  4. MENU",24,84,14,Color.LTGRAY);

            fill(c,Color.rgb(26,29,32),20,105,1260,170);
            text(c,"ACTIVITY KEY EVENT",36,130,12,Color.rgb(235,127,25));
            text(c,lastActivityKey,36,156,14,Color.WHITE);

            fill(c,Color.rgb(26,29,32),20,185,1260,250);
            text(c,"ACCESSIBILITY GLOBAL KEY EVENT",36,210,12,Color.rgb(235,127,25));
            text(c,MainActivity.lastGlobalKey,36,236,14,Color.WHITE);

            fill(c,Color.rgb(26,29,32),20,265,1260,350);
            text(c,"BROADCAST / MCU PROBE",36,290,12,Color.rgb(235,127,25));

            String b = MainActivity.lastBroadcast;
            if (b.length() > 125) b = b.substring(0,125) + "...";
            text(c,b,36,318,13,Color.WHITE);

            fill(c,Color.rgb(26,29,32),20,365,1260,430);
            text(c,"MOTION EVENT",36,390,12,Color.rgb(235,127,25));
            text(c,lastMotion,36,416,13,Color.WHITE);

            text(c,"Daca GLOBAL KEY ramane gol, activeaza serviciul F01 iDrive Diagnostic in Accessibility.",
                    24,463,12,Color.LTGRAY);
        }

        @Override public boolean onGenericMotionEvent(MotionEvent e) {
            float v=e.getAxisValue(MotionEvent.AXIS_VSCROLL);
            float h=e.getAxisValue(MotionEvent.AXIS_HSCROLL);
            float s=e.getAxisValue(MotionEvent.AXIS_SCROLL);
            lastMotion="src="+e.getSource()+" action="+e.getAction()+
                    " v="+v+" h="+h+" scroll="+s;
            invalidate();
            return true;
        }

        @Override public boolean onTouchEvent(MotionEvent e) {
            if (e.getAction()==MotionEvent.ACTION_UP) {
                // tap lower-right opens Accessibility settings
                float x=e.getX()/sx(), y=e.getY()/sy();
                if (x>900 && y>430) {
                    try {
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    } catch(Exception ignored){}
                    return true;
                }
            }
            return true;
        }
    }
}
