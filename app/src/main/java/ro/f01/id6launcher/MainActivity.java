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
        Bitmap bg;

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
        int page = 0; // 0=NBT, 1=Apps

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

            BitmapDrawable d =
                    (BitmapDrawable) getResources().getDrawable(
                            R.drawable.nbt_home
                    );

            bg = d.getBitmap();

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

        void drawNBT(Canvas c) {

            Rect src =
                    new Rect(
                            0,
                            0,
                            bg.getWidth(),
                            bg.getHeight()
                    );

            Rect dst =
                    new Rect(
                            0,
                            0,
                            getWidth(),
                            getHeight()
                    );

            c.drawBitmap(
                    bg,
                    src,
                    dst,
                    p
            );

            /*
             * MASCAM meniul static din imagine si il redesenam mai mic,
             * pentru ca selectia sa fie dinamica si controlabila din iDrive.
             */
            fill(
                    c,
                    Color.rgb(5, 6, 7),
                    355,
                    82,
                    925,
                    425
            );

            /*
             * Ora si data reale.
             */
            fill(
                    c,
                    Color.argb(235, 5, 6, 7),
                    785,
                    18,
                    1115,
                    70
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
                    930,
                    49,
                    13,
                    Color.LTGRAY,
                    Paint.Align.RIGHT
            );

            text(
                    c,
                    time,
                    1075,
                    51,
                    21,
                    Color.WHITE,
                    Paint.Align.RIGHT
            );

            /*
             * Meniu NBT: mai mic si mai aerisit.
             */
            float top = 112f;
            float rowH = 34f;

            for (int i = 0; i < menu.length; i++) {

                float y =
                        top +
                        i * rowH;

                if (i == selected) {

                    fill(
                            c,
                            Color.rgb(42, 30, 20),
                            370,
                            y - 24,
                            915,
                            y + 7
                    );

                    fill(
                            c,
                            Color.rgb(235, 127, 25),
                            370,
                            y - 24,
                            915,
                            y - 21
                    );
                }

                text(
                        c,
                        menu[i],
                        397,
                        y,
                        17,
                        i == selected
                                ? Color.WHITE
                                : Color.rgb(195, 198, 200),
                        Paint.Align.LEFT
                );
            }

            /*
             * Zona de jos.
             */
            fill(
                    c,
                    Color.argb(220, 5, 6, 7),
                    880,
                    427,
                    1270,
                    479
            );

            text(
                    c,
                    "iDrive rotire = selectie",
                    1240,
                    453,
                    11,
                    Color.LTGRAY,
                    Paint.Align.RIGHT
            );

            text(
                    c,
                    "Touch = OK",
                    1240,
                    470,
                    11,
                    Color.rgb(235, 127, 25),
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
         * iDrive confirmat pe HU:
         * MEDIA_PREVIOUS = 88
         * MEDIA_NEXT     = 87
         *
         * Le folosim pentru selectie.
         */
        boolean handleKey(KeyEvent e) {

            if (e.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            int code = e.getKeyCode();

            if (
                    code == KeyEvent.KEYCODE_MEDIA_PREVIOUS ||
                    code == 88
            ) {

                if (page == 0) {
                    moveSelection(-1);
                }

                return true;
            }

            if (
                    code == KeyEvent.KEYCODE_MEDIA_NEXT ||
                    code == 87
            ) {

                if (page == 0) {
                    moveSelection(1);
                }

                return true;
            }

            /*
             * Daca HU trimite vreodata ENTER/DPAD_CENTER,
             * codul este deja pregatit.
             */
            if (
                    code == KeyEvent.KEYCODE_DPAD_CENTER ||
                    code == KeyEvent.KEYCODE_ENTER
            ) {

                if (page == 0) {
                    openSelected();
                }

                return true;
            }

            if (
                    code == KeyEvent.KEYCODE_BACK ||
                    code == KeyEvent.KEYCODE_ESCAPE
            ) {

                if (page != 0) {

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

            for (ResolveInfo ri : list) {

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
                            instanceof BitmapDrawable
            ) {

                return Bitmap
                        .createScaledBitmap(
                                ((BitmapDrawable) drawable)
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
                    18,
                    Color.WHITE,
                    Paint.Align.LEFT
            );

            text(
                    c,
                    "‹ HOME",
                    1240,
                    33,
                    13,
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

                } catch (Exception ignored) {}

                String label =
                        apps.get(i).label;

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
                        12,
                        Color.WHITE,
                        Paint.Align.LEFT
                );
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {

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

                    /*
                     * Touch pe meniul central.
                     * Primul tap selecteaza, al doilea deschide.
                     */
                    if (
                            x >= 355
                            &&
                            x <= 930
                            &&
                            y >= 80
                            &&
                            y <= 430
                    ) {

                        int idx =
                                (int)
                                (
                                    (y - 88)
                                    / 34f
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
