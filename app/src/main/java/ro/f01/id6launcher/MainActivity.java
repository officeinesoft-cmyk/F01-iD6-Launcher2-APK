package ro.f01.id6launcher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.provider.Settings;
import android.view.*;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    NbtView view;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        hideBars();

        view = new NbtView(this);
        setContentView(view);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideBars();
    }

    void hideBars() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (view != null && view.handleKey(e)) {
            return true;
        }
        return super.dispatchKeyEvent(e);
    }

    void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    void openFirst(String[] packages, Intent fallback) {
        PackageManager pm = getPackageManager();

        for (String pkg : packages) {
            try {
                Intent i = pm.getLaunchIntentForPackage(pkg);
                if (i != null) {
                    startActivity(i);
                    return;
                }
            } catch (Exception ignored) {}
        }

        try {
            startActivity(fallback);
        } catch (Exception e) {
            toast("Aplicatia nu este disponibila");
        }
    }

    void openNavigation() {
        openFirst(
                new String[]{
                        "com.waze",
                        "com.google.android.apps.maps"
                },
                new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=Bucharest")
                )
        );
    }

    void openMedia() {
        openFirst(
                new String[]{
                        "com.spotify.music",
                        "com.google.android.apps.youtube.music",
                        "com.maxmpz.audioplayer"
                },
                new Intent("android.intent.action.MUSIC_PLAYER")
        );
    }

    void openPhone() {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL));
        } catch (Exception e) {
            toast("Telefon indisponibil");
        }
    }

    void openSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        } catch (Exception ignored) {}
    }

    class AppEntry {
        String label;
        Drawable icon;
        Intent intent;
    }

    class NbtView extends View {

        final float BW = 1280f;
        final float BH = 480f;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        Handler handler = new Handler();

        String[] menu = {
                "Multimedia",
                "Radio",
                "Telephone",
                "Navigation",
                "Office",
                "ConnectedDrive",
                "Vehicle information",
                "Settings",
                "Apps"
        };

        int selected = 0;
        int page = 0; // 0 NBT, 1 Apps

        ArrayList<AppEntry> apps = new ArrayList<>();

        float appScroll = 0f;
        float downX, downY, lastY;
        boolean verticalScrolling = false;

        Runnable tick = new Runnable() {
            @Override
            public void run() {
                invalidate();
                handler.postDelayed(this, 1000);
            }
        };

        NbtView(Context c) {
            super(c);

            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();

            loadApps();

            handler.post(tick);
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            handler.removeCallbacks(tick);
        }

        float sx() {
            return getWidth() / BW;
        }

        float sy() {
            return getHeight() / BH;
        }

        void fill(
                Canvas c,
                int color,
                float l,
                float t,
                float r,
                float b
        ) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);

            c.drawRect(
                    l * sx(),
                    t * sy(),
                    r * sx(),
                    b * sy(),
                    p
            );
        }

        void line(
                Canvas c,
                int color,
                float sw,
                float x1,
                float y1,
                float x2,
                float y2
        ) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(sw * sx());
            p.setColor(color);

            c.drawLine(
                    x1 * sx(),
                    y1 * sy(),
                    x2 * sx(),
                    y2 * sy(),
                    p
            );

            p.setStyle(Paint.Style.FILL);
        }

        void text(
                Canvas c,
                String s,
                float x,
                float y,
                float size,
                int color,
                Paint.Align align
        ) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);

            p.setTextAlign(align);

            p.setTypeface(
                    Typeface.create(
                            "sans",
                            Typeface.NORMAL
                    )
            );

            p.setTextSize(
                    size * sy()
            );

            c.drawText(
                    s,
                    x * sx(),
                    y * sy(),
                    p
            );
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);

            if (page == 0) {
                drawNBT(c);
            } else {
                drawApps(c);
            }
        }

        void drawBMWRoundel(Canvas c, float cx, float cy, float r) {
            // outer rings
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(20,20,20));
            c.drawCircle(cx*sx(), cy*sy(), r*sx(), p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3*sx());
            p.setColor(Color.rgb(175,175,175));
            c.drawCircle(cx*sx(), cy*sy(), (r-4)*sx(), p);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.BLACK);
            c.drawCircle(cx*sx(), cy*sy(), (r-12)*sx(), p);

            float inner = r - 23;

            RectF q = new RectF(
                    (cx-inner)*sx(),
                    (cy-inner)*sy(),
                    (cx+inner)*sx(),
                    (cy+inner)*sy()
            );

            p.setColor(Color.rgb(235,235,235));
            c.drawArc(q, 0, 90, true, p);
            c.drawArc(q, 180, 90, true, p);

            p.setColor(Color.rgb(40,145,205));
            c.drawArc(q, 90, 90, true, p);
            c.drawArc(q, 270, 90, true, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2*sx());
            p.setColor(Color.rgb(220,220,220));
            c.drawCircle(cx*sx(), cy*sy(), inner*sx(), p);

            p.setStyle(Paint.Style.FILL);
            text(c, "B", cx-28, cy-r+22, 12, Color.WHITE, Paint.Align.CENTER);
            text(c, "M", cx, cy-r+17, 12, Color.WHITE, Paint.Align.CENTER);
            text(c, "W", cx+28, cy-r+22, 12, Color.WHITE, Paint.Align.CENTER);
        }

        void drawNBT(Canvas c) {

            // background
            c.drawColor(Color.BLACK);

            fill(
                    c,
                    Color.rgb(8, 9, 10),
                    0,
                    0,
                    1280,
                    480
            );

            // subtle vertical split
            fill(
                    c,
                    Color.rgb(10, 11, 12),
                    0,
                    0,
                    365,
                    480
            );

            fill(
                    c,
                    Color.rgb(6, 7, 8),
                    365,
                    0,
                    960,
                    480
            );

            fill(
                    c,
                    Color.rgb(9, 10, 11),
                    960,
                    0,
                    1280,
                    480
            );

            // header
            line(
                    c,
                    Color.rgb(70,70,70),
                    1,
                    340,
                    58,
                    1235,
                    58
            );

            text(
                    c,
                    "Main menu",
                    385,
                    42,
                    18,
                    Color.WHITE,
                    Paint.Align.LEFT
            );

            String time =
                    new SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                    ).format(new Date());

            String date =
                    new SimpleDateFormat(
                            "dd.MM.yyyy",
                            new Locale("ro", "RO")
                    ).format(new Date());

            text(
                    c,
                    date,
                    1025,
                    41,
                    12,
                    Color.LTGRAY,
                    Paint.Align.RIGHT
            );

            text(
                    c,
                    time,
                    1175,
                    43,
                    19,
                    Color.WHITE,
                    Paint.Align.RIGHT
            );

            // decorative NBT wave lines
            for (int i = 0; i < 9; i++) {
                float yy = 305 + i * 12;

                line(
                        c,
                        Color.rgb(45,45,45),
                        1,
                        0,
                        yy,
                        300 + i*6,
                        270 + i*4
                );

                line(
                        c,
                        Color.rgb(35,35,35),
                        1,
                        980,
                        300 + i*4,
                        1280,
                        yy
                );
            }

            // BMW selector
            drawBMWRoundel(
                    c,
                    275,
                    244,
                    73
            );

            line(
                    c,
                    Color.rgb(235,127,25),
                    2,
                    328,
                    205,
                    378,
                    145
            );

            // Menu
            float top = 102f;
            float rowH = 37f;

            for (int i = 0; i < menu.length; i++) {

                float y =
                        top +
                        i * rowH;

                if (i == selected) {

                    fill(
                            c,
                            Color.rgb(31, 20, 12),
                            380,
                            y - 24,
                            900,
                            y + 7
                    );

                    line(
                            c,
                            Color.rgb(235,127,25),
                            3,
                            380,
                            y - 23,
                            900,
                            y - 23
                    );
                }

                line(
                        c,
                        Color.rgb(35,35,35),
                        1,
                        380,
                        y + 11,
                        915,
                        y + 11
                );

                text(
                        c,
                        menu[i],
                        402,
                        y,
                        19,
                        i == selected
                                ? Color.WHITE
                                : Color.rgb(195,198,200),
                        Paint.Align.LEFT
                );
            }

            // right context panel
            line(
                    c,
                    Color.rgb(55,55,55),
                    1,
                    945,
                    58,
                    945,
                    440
            );

            String title = menu[selected];

            if (
                    selected == 0 ||
                    selected == 1
            ) {
                text(c,"♪",1086,250,76,Color.rgb(95,95,95),Paint.Align.CENTER);
                text(c,"Media",1086,330,16,Color.LTGRAY,Paint.Align.CENTER);
            }

            else if (
                    selected == 2
            ) {
                text(c,"☎",1086,245,68,Color.rgb(95,95,95),Paint.Align.CENTER);
                text(c,"Telephone",1086,330,16,Color.LTGRAY,Paint.Align.CENTER);
            }

            else if (
                    selected == 3
            ) {
                text(c,"▲",1086,245,68,Color.rgb(95,95,95),Paint.Align.CENTER);
                text(c,"Navigation",1086,330,16,Color.LTGRAY,Paint.Align.CENTER);
            }

            else if (
                    selected == 6
            ) {
                text(c,"BMW 7 Series",1086,225,20,Color.WHITE,Paint.Align.CENTER);
                text(c,"F01 2011 pre-LCI",1086,260,14,Color.LTGRAY,Paint.Align.CENTER);
                text(c,"DJ 10 SDS",1086,297,17,Color.WHITE,Paint.Align.CENTER);
            }

            else if (
                    selected == 8
            ) {
                text(c,"▦",1086,245,66,Color.rgb(95,95,95),Paint.Align.CENTER);
                text(c,"Android Apps",1086,330,16,Color.LTGRAY,Paint.Align.CENTER);
            }

            else {
                text(c,title,1086,250,18,Color.LTGRAY,Paint.Align.CENTER);
            }

            // footer hint
            text(
                    c,
                    "iDrive rotire = selectie",
                    28,
                    460,
                    11,
                    Color.rgb(150,150,150),
                    Paint.Align.LEFT
            );

            text(
                    c,
                    "Touch = OK",
                    1240,
                    460,
                    11,
                    Color.rgb(235,127,25),
                    Paint.Align.RIGHT
            );
        }

        void moveSelection(int delta) {

            selected += delta;

            if (selected < 0) {
                selected = menu.length - 1;
            }

            if (selected >= menu.length) {
                selected = 0;
            }

            invalidate();
        }

        void openSelected() {

            switch (selected) {

                case 0:
                case 1:
                    openMedia();
                    break;

                case 2:
                    openPhone();
                    break;

                case 3:
                    openNavigation();
                    break;

                case 4:
                    toast("Office");
                    break;

                case 5:
                    toast("ConnectedDrive - integrare CAN in lucru");
                    break;

                case 6:
                    toast(
                            "BMW F01 2011 pre-LCI • DJ 10 SDS"
                    );
                    break;

                case 7:
                    openSettings();
                    break;

                case 8:
                    page = 1;
                    invalidate();
                    break;
            }
        }

        /*
         * Confirmat pe HU:
         * 88 = MEDIA_PREVIOUS
         * 87 = MEDIA_NEXT
         */
        boolean handleKey(KeyEvent e) {

            if (
                    e.getAction()
                            !=
                    KeyEvent.ACTION_DOWN
            ) {

                return false;
            }

            int code =
                    e.getKeyCode();

            if (
                    code == 88 ||
                    code == KeyEvent.KEYCODE_MEDIA_PREVIOUS
            ) {

                if (
                        page == 0
                ) {

                    moveSelection(
                            -1
                    );

                    return true;
                }

                if (
                        page == 1
                ) {

                    appScroll -= 44f;

                    if (
                            appScroll < 0
                    ) {

                        appScroll = 0;
                    }

                    invalidate();

                    return true;
                }
            }

            if (
                    code == 87 ||
                    code == KeyEvent.KEYCODE_MEDIA_NEXT
            ) {

                if (
                        page == 0
                ) {

                    moveSelection(
                            1
                    );

                    return true;
                }

                if (
                        page == 1
                ) {

                    appScroll += 44f;

                    float contentHeight =
                            ((apps.size() + 5) / 6)
                                    * 88f;

                    float maxScroll =
                            Math.max(
                                    0,
                                    contentHeight
                                            - 400f
                            );

                    if (
                            appScroll > maxScroll
                    ) {

                        appScroll =
                                maxScroll;
                    }

                    invalidate();

                    return true;
                }
            }

            if (
                    code == KeyEvent.KEYCODE_BACK ||
                    code == KeyEvent.KEYCODE_ESCAPE
            ) {

                if (
                        page != 0
                ) {

                    page = 0;
                    invalidate();

                    return true;
                }
            }

            return false;
        }

        void loadApps() {

            apps.clear();

            PackageManager pm =
                    getPackageManager();

            Intent q =
                    new Intent(
                            Intent.ACTION_MAIN,
                            null
                    );

            q.addCategory(
                    Intent.CATEGORY_LAUNCHER
            );

            List<ResolveInfo> list =
                    pm.queryIntentActivities(
                            q,
                            0
                    );

            Collections.sort(
                    list,
                    new Comparator<ResolveInfo>() {
                        @Override
                        public int compare(
                                ResolveInfo a,
                                ResolveInfo b
                        ) {

                            return a.loadLabel(pm)
                                    .toString()
                                    .compareToIgnoreCase(
                                            b.loadLabel(pm)
                                                    .toString()
                                    );
                        }
                    }
            );

            for (
                    ResolveInfo ri :
                    list
            ) {

                if (
                        ri.activityInfo.packageName
                                .equals(
                                        getPackageName()
                                )
                ) {

                    continue;
                }

                AppEntry a =
                        new AppEntry();

                a.label =
                        ri.loadLabel(pm)
                                .toString();

                a.icon =
                        ri.loadIcon(pm);

                a.intent =
                        new Intent(
                                Intent.ACTION_MAIN
                        );

                a.intent.addCategory(
                        Intent.CATEGORY_LAUNCHER
                );

                a.intent.setComponent(
                        new ComponentName(
                                ri.activityInfo.packageName,
                                ri.activityInfo.name
                        )
                );

                a.intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                apps.add(a);
            }
        }

        Bitmap iconBitmap(
                Drawable drawable,
                int width,
                int height
        ) {

            if (
                    drawable
                            instanceof
                    BitmapDrawable
            ) {

                return Bitmap
                        .createScaledBitmap(
                                ((BitmapDrawable)
                                        drawable)
                                        .getBitmap(),
                                width,
                                height,
                                true
                        );
            }

            Bitmap bitmap =
                    Bitmap.createBitmap(
                            width,
                            height,
                            Bitmap.Config.ARGB_8888
                    );

            Canvas canvas =
                    new Canvas(bitmap);

            drawable.setBounds(
                    0,
                    0,
                    width,
                    height
            );

            drawable.draw(
                    canvas
            );

            return bitmap;
        }

        void drawApps(Canvas c) {

            c.drawColor(
                    Color.rgb(
                            5,
                            6,
                            7
                    )
            );

            fill(
                    c,
                    Color.rgb(
                            17,
                            19,
                            21
                    ),
                    0,
                    0,
                    1280,
                    50
            );

            text(
                    c,
                    "APPS",
                    24,
                    33,
                    17,
                    Color.WHITE,
                    Paint.Align.LEFT
            );

            text(
                    c,
                    "‹ HOME",
                    1240,
                    33,
                    12,
                    Color.LTGRAY,
                    Paint.Align.RIGHT
            );

            int columns = 6;

            float cellWidth =
                    1280f /
                    columns;

            float cellHeight =
                    88f;

            float top =
                    61f -
                    appScroll;

            for (
                    int i = 0;
                    i < apps.size();
                    i++
            ) {

                int row =
                        i /
                        columns;

                int column =
                        i %
                        columns;

                float left =
                        column *
                        cellWidth +
                        9;

                float y =
                        top +
                        row *
                        cellHeight;

                float right =
                        (column + 1)
                        * cellWidth -
                        9;

                float bottom =
                        y + 76;

                if (
                        bottom < 50 ||
                        y > 480
                ) {

                    continue;
                }

                fill(
                        c,
                        Color.rgb(
                                18,
                                20,
                                22
                        ),
                        left,
                        y,
                        right,
                        bottom
                );

                try {

                    Bitmap icon =
                            iconBitmap(
                                    apps.get(i).icon,
                                    36,
                                    36
                            );

                    Rect src =
                            new Rect(
                                    0,
                                    0,
                                    icon.getWidth(),
                                    icon.getHeight()
                            );

                    RectF dest =
                            new RectF(
                                    (left + 9) * sx(),
                                    (y + 17) * sy(),
                                    (left + 45) * sx(),
                                    (y + 53) * sy()
                            );

                    c.drawBitmap(
                            icon,
                            src,
                            dest,
                            p
                    );

                } catch (
                        Exception ignored
                ) {}

                String label =
                        apps.get(i)
                                .label;

                if (
                        label.length() > 15
                ) {

                    label =
                            label.substring(
                                    0,
                                    14
                            ) + "…";
                }

                text(
                        c,
                        label,
                        left + 55,
                        y + 41,
                        11,
                        Color.WHITE,
                        Paint.Align.LEFT
                );
            }
        }

        @Override
        public boolean onTouchEvent(
                MotionEvent e
        ) {

            float x =
                    e.getX() /
                    sx();

            float y =
                    e.getY() /
                    sy();

            if (
                    e.getAction()
                            ==
                    MotionEvent.ACTION_DOWN
            ) {

                downX = x;
                downY = y;
                lastY = y;

                verticalScrolling =
                        false;

                return true;
            }

            if (
                    e.getAction()
                            ==
                    MotionEvent.ACTION_MOVE
            ) {

                float dx =
                        x - downX;

                float dy =
                        y - downY;

                if (
                        page == 1
                        &&
                        Math.abs(dy)
                                >
                        Math.abs(dx)
                        &&
                        Math.abs(dy) > 8
                ) {

                    verticalScrolling =
                            true;

                    appScroll +=
                            lastY - y;

                    float contentHeight =
                            ((apps.size() + 5) / 6)
                                    * 88f;

                    float maxScroll =
                            Math.max(
                                    0,
                                    contentHeight
                                            - 400f
                            );

                    if (
                            appScroll < 0
                    ) {

                        appScroll = 0;
                    }

                    if (
                            appScroll > maxScroll
                    ) {

                        appScroll =
                                maxScroll;
                    }

                    lastY = y;

                    invalidate();
                }

                return true;
            }

            if (
                    e.getAction()
                            ==
                    MotionEvent.ACTION_UP
            ) {

                if (
                        page == 0
                ) {

                    if (
                            x >= 370
                            &&
                            x <= 915
                            &&
                            y >= 75
                            &&
                            y <= 430
                    ) {

                        int idx =
                                (int)
                                (
                                    (y - 78)
                                    / 37f
                                );

                        if (
                                idx >= 0
                                &&
                                idx <
                                menu.length
                        ) {

                            if (
                                    idx ==
                                    selected
                            ) {

                                openSelected();

                            } else {

                                selected =
                                        idx;

                                invalidate();
                            }

                            return true;
                        }
                    }

                } else {

                    if (
                            x > 1080
                            &&
                            y < 60
                    ) {

                        page = 0;
                        invalidate();

                        return true;
                    }

                    int columns = 6;

                    float cellWidth =
                            1280f /
                            columns;

                    float adjustedY =
                            y
                            - 61f
                            + appScroll;

                    if (
                            adjustedY >= 0
                    ) {

                        int column =
                                (int)
                                (
                                    x /
                                    cellWidth
                                );

                        int row =
                                (int)
                                (
                                    adjustedY /
                                    88f
                                );

                        int index =
                                row
                                * columns
                                + column;

                        if (
                                index >= 0
                                &&
                                index <
                                apps.size()
                        ) {

                            try {

                                startActivity(
                                        apps.get(index)
                                                .intent
                                );

                            } catch (
                                    Exception ex
                            ) {

                                toast(
                                        "Nu pot porni aplicatia"
                                );
                            }

                            return true;
                        }
                    }
                }
            }

            return true;
        }
    }
}
