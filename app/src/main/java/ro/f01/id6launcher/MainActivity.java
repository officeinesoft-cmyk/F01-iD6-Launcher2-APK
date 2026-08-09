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

    CustomView view;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        hideBars();

        view = new CustomView(this);
        setContentView(view);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideBars();
    }

    private void hideBars() {
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
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (view != null && view.handleKey(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void openFirstAvailable(String[] packages, Intent fallback) {
        PackageManager pm = getPackageManager();

        for (String pkg : packages) {
            try {
                Intent intent = pm.getLaunchIntentForPackage(pkg);
                if (intent != null) {
                    startActivity(intent);
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

    private void openNavigation() {
        openFirstAvailable(
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

    private void openMedia() {
        openFirstAvailable(
                new String[]{
                        "com.spotify.music",
                        "com.google.android.apps.youtube.music",
                        "com.maxmpz.audioplayer"
                },
                new Intent("android.intent.action.MUSIC_PLAYER")
        );
    }

    private void openPhone() {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL));
        } catch (Exception e) {
            toast("Telefon indisponibil");
        }
    }

    private void openSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        } catch (Exception ignored) {}
    }

    class AppEntry {
        String label;
        Drawable icon;
        Intent intent;
    }

    class CustomView extends View {

        final float BASE_W = 1280f;
        final float BASE_H = 480f;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap home;

        Handler handler = new Handler();

        String[] menu = {
                "Navigation",
                "Media/Multimedia",
                "Telephone",
                "Vehicle",
                "Apps",
                "Settings"
        };

        int selected = 0;
        int page = 0; // 0 = HOME, 1 = APPS

        ArrayList<AppEntry> apps = new ArrayList<>();

        float appScroll = 0f;
        float downX, downY, lastY;
        boolean verticalScrolling = false;

        Runnable clockTick = new Runnable() {
            @Override
            public void run() {
                invalidate();
                handler.postDelayed(this, 1000);
            }
        };

        CustomView(Context context) {
            super(context);

            BitmapDrawable drawable =
                    (BitmapDrawable) getResources().getDrawable(
                            R.drawable.custom_home_v9_final
                    );

            home = drawable.getBitmap();

            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();

            loadApps();

            handler.post(clockTick);
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            handler.removeCallbacks(clockTick);
        }

        float sx() {
            return getWidth() / BASE_W;
        }

        float sy() {
            return getHeight() / BASE_H;
        }

        void fill(
                Canvas canvas,
                int color,
                float left,
                float top,
                float right,
                float bottom
        ) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);

            canvas.drawRect(
                    left * sx(),
                    top * sy(),
                    right * sx(),
                    bottom * sy(),
                    paint
            );
        }

        void text(
                Canvas canvas,
                String text,
                float x,
                float y,
                float size,
                int color,
                Paint.Align align
        ) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            paint.setTextAlign(align);
            paint.setTypeface(Typeface.create("sans", Typeface.NORMAL));
            paint.setTextSize(size * sy());

            canvas.drawText(
                    text,
                    x * sx(),
                    y * sy(),
                    paint
            );
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (page == 0) {
                drawHome(canvas);
            } else {
                drawApps(canvas);
            }
        }

        void drawHome(Canvas canvas) {

            Rect source = new Rect(
                    0, 0,
                    home.getWidth(),
                    home.getHeight()
            );

            Rect destination = new Rect(
                    0, 0,
                    getWidth(),
                    getHeight()
            );

            canvas.drawBitmap(
                    home,
                    source,
                    destination,
                    paint
            );

            // Real clock/date over the static mockup values.
            fill(
                    canvas,
                    Color.argb(235, 5, 6, 7),
                    595, 0,
                    1280, 56
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
                    canvas,
                    time,
                    720,
                    37,
                    20,
                    Color.WHITE,
                    Paint.Align.RIGHT
            );

            text(
                    canvas,
                    date,
                    1245,
                    37,
                    16,
                    Color.WHITE,
                    Paint.Align.RIGHT
            );

            /*
             * Dynamic menu overlay.
             * Keeps the approved visual background/car, while selection is live.
             */
            fill(
                    canvas,
                    Color.argb(220, 4, 5, 6),
                    0, 72,
                    325, 420
            );

            float top = 116f;
            float rowHeight = 53f;

            for (int i = 0; i < menu.length; i++) {

                float y = top + i * rowHeight;

                if (i == selected) {
                    fill(
                            canvas,
                            Color.argb(225, 48, 27, 10),
                            0,
                            y - 35,
                            315,
                            y + 10
                    );

                    fill(
                            canvas,
                            Color.rgb(235, 127, 25),
                            0,
                            y - 35,
                            5,
                            y + 10
                    );
                }

                String marker;

                switch (i) {
                    case 0:
                        marker = "➤";
                        break;
                    case 1:
                        marker = "♪";
                        break;
                    case 2:
                        marker = "☎";
                        break;
                    case 3:
                        marker = "●";
                        break;
                    case 4:
                        marker = "▦";
                        break;
                    default:
                        marker = "⚙";
                        break;
                }

                text(
                        canvas,
                        marker,
                        38,
                        y,
                        18,
                        i == selected
                                ? Color.rgb(235, 127, 25)
                                : Color.WHITE,
                        Paint.Align.CENTER
                );

                text(
                        canvas,
                        menu[i],
                        75,
                        y,
                        18,
                        i == selected
                                ? Color.WHITE
                                : Color.rgb(220, 220, 220),
                        Paint.Align.LEFT
                );
            }

            /*
             * Live CAN data is not faked.
             * Hide the mockup demo values and show placeholders until we read them.
             */
            fill(
                    canvas,
                    Color.argb(230, 6, 7, 8),
                    1080, 75,
                    1280, 395
            );

            text(
                    canvas,
                    "OUTSIDE",
                    1110,
                    112,
                    12,
                    Color.LTGRAY,
                    Paint.Align.LEFT
            );

            text(
                    canvas,
                    "-- °C",
                    1110,
                    145,
                    20,
                    Color.WHITE,
                    Paint.Align.LEFT
            );

            fill(
                    canvas,
                    Color.rgb(55, 55, 55),
                    1100, 165,
                    1260, 166
            );

            text(
                    canvas,
                    "FUEL RANGE",
                    1110,
                    205,
                    12,
                    Color.LTGRAY,
                    Paint.Align.LEFT
            );

            text(
                    canvas,
                    "-- km",
                    1110,
                    238,
                    20,
                    Color.WHITE,
                    Paint.Align.LEFT
            );

            fill(
                    canvas,
                    Color.rgb(55, 55, 55),
                    1100, 258,
                    1260, 259
            );

            text(
                    canvas,
                    "SPEED",
                    1110,
                    300,
                    12,
                    Color.LTGRAY,
                    Paint.Align.LEFT
            );

            text(
                    canvas,
                    "-- km/h",
                    1110,
                    333,
                    20,
                    Color.WHITE,
                    Paint.Align.LEFT
            );

            // Footer hints.
            fill(
                    canvas,
                    Color.argb(220, 5, 6, 7),
                    0, 420,
                    1280, 480
            );

            text(
                    canvas,
                    "iDrive rotire = selectie",
                    28,
                    456,
                    11,
                    Color.LTGRAY,
                    Paint.Align.LEFT
            );

            text(
                    canvas,
                    "Touch = OK",
                    1240,
                    456,
                    11,
                    Color.rgb(235, 127, 25),
                    Paint.Align.RIGHT
            );
        }

        /*
         * IMPORTANT: diagnostic on this HU showed iDrive rotation arrives on KEY UP:
         * 88 = MEDIA_PREVIOUS
         * 87 = MEDIA_NEXT
         */
        boolean handleKey(KeyEvent event) {

            if (event.getAction() != KeyEvent.ACTION_UP) {
                return false;
            }

            int code = event.getKeyCode();

            if (
                    code == 88 ||
                    code == KeyEvent.KEYCODE_MEDIA_PREVIOUS
            ) {

                if (page == 0) {
                    selected--;

                    if (selected < 0) {
                        selected = menu.length - 1;
                    }

                    invalidate();
                } else {
                    scrollApps(-1);
                }

                return true;
            }

            if (
                    code == 87 ||
                    code == KeyEvent.KEYCODE_MEDIA_NEXT
            ) {

                if (page == 0) {
                    selected++;

                    if (selected >= menu.length) {
                        selected = 0;
                    }

                    invalidate();
                } else {
                    scrollApps(1);
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

        void openSelected() {

            switch (selected) {

                case 0:
                    openNavigation();
                    break;

                case 1:
                    openMedia();
                    break;

                case 2:
                    openPhone();
                    break;

                case 3:
                    toast("BMW F01 2011 • Cashmere Silver • DJ 10 SDS");
                    break;

                case 4:
                    page = 1;
                    invalidate();
                    break;

                case 5:
                    openSettings();
                    break;
            }
        }

        void scrollApps(int direction) {

            appScroll += direction * 44f;

            float contentHeight =
                    ((apps.size() + 4) / 5) * 105f;

            float maxScroll =
                    Math.max(
                            0,
                            contentHeight - 390f
                    );

            if (appScroll < 0) {
                appScroll = 0;
            }

            if (appScroll > maxScroll) {
                appScroll = maxScroll;
            }

            invalidate();
        }

        void loadApps() {

            apps.clear();

            PackageManager pm =
                    getPackageManager();

            Intent query =
                    new Intent(
                            Intent.ACTION_MAIN,
                            null
                    );

            query.addCategory(
                    Intent.CATEGORY_LAUNCHER
            );

            List<ResolveInfo> list =
                    pm.queryIntentActivities(
                            query,
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

                AppEntry entry =
                        new AppEntry();

                entry.label =
                        ri.loadLabel(pm)
                                .toString();

                entry.icon =
                        ri.loadIcon(pm);

                entry.intent =
                        new Intent(
                                Intent.ACTION_MAIN
                        );

                entry.intent.addCategory(
                        Intent.CATEGORY_LAUNCHER
                );

                entry.intent.setComponent(
                        new ComponentName(
                                ri.activityInfo.packageName,
                                ri.activityInfo.name
                        )
                );

                entry.intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                apps.add(entry);
            }
        }

        Bitmap iconBitmap(
                Drawable drawable,
                int width,
                int height
        ) {

            if (
                    drawable instanceof
                    BitmapDrawable
            ) {

                return Bitmap.createScaledBitmap(
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
                    0, 0,
                    width, height
            );

            drawable.draw(canvas);

            return bitmap;
        }

        void drawApps(Canvas canvas) {

            canvas.drawColor(
                    Color.rgb(
                            4, 5, 6
                    )
            );

            fill(
                    canvas,
                    Color.rgb(
                            16, 18, 20
                    ),
                    0, 0,
                    1280, 52
            );

            text(
                    canvas,
                    "APPS",
                    25,
                    35,
                    18,
                    Color.WHITE,
                    Paint.Align.LEFT
            );

            text(
                    canvas,
                    "‹ HOME",
                    1240,
                    35,
                    13,
                    Color.LTGRAY,
                    Paint.Align.RIGHT
            );

            int columns = 5;

            float cellWidth =
                    1280f / columns;

            float cellHeight =
                    105f;

            float top =
                    70f - appScroll;

            for (
                    int i = 0;
                    i < apps.size();
                    i++
            ) {

                int row =
                        i / columns;

                int column =
                        i % columns;

                float left =
                        column * cellWidth + 12;

                float y =
                        top + row * cellHeight;

                float right =
                        (column + 1) * cellWidth - 12;

                float bottom =
                        y + 92;

                if (
                        bottom < 52 ||
                        y > 480
                ) {
                    continue;
                }

                fill(
                        canvas,
                        Color.rgb(
                                19, 21, 23
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
                                    42,
                                    42
                            );

                    Rect source =
                            new Rect(
                                    0, 0,
                                    icon.getWidth(),
                                    icon.getHeight()
                            );

                    RectF destination =
                            new RectF(
                                    (left + 15) * sx(),
                                    (y + 18) * sy(),
                                    (left + 57) * sx(),
                                    (y + 60) * sy()
                            );

                    canvas.drawBitmap(
                            icon,
                            source,
                            destination,
                            paint
                    );

                } catch (Exception ignored) {}

                String label =
                        apps.get(i).label;

                if (label.length() > 18) {
                    label =
                            label.substring(
                                    0,
                                    17
                            ) + "…";
                }

                text(
                        canvas,
                        label,
                        left + 72,
                        y + 48,
                        13,
                        Color.WHITE,
                        Paint.Align.LEFT
                );
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            float x =
                    event.getX() / sx();

            float y =
                    event.getY() / sy();

            if (
                    event.getAction()
                            ==
                    MotionEvent.ACTION_DOWN
            ) {

                downX = x;
                downY = y;
                lastY = y;
                verticalScrolling = false;

                return true;
            }

            if (
                    event.getAction()
                            ==
                    MotionEvent.ACTION_MOVE
            ) {

                float dx =
                        x - downX;

                float dy =
                        y - downY;

                if (
                        page == 1 &&
                        Math.abs(dy) > Math.abs(dx) &&
                        Math.abs(dy) > 8
                ) {

                    verticalScrolling = true;

                    appScroll += lastY - y;

                    float contentHeight =
                            ((apps.size() + 4) / 5) * 105f;

                    float maxScroll =
                            Math.max(
                                    0,
                                    contentHeight - 390f
                            );

                    if (appScroll < 0) {
                        appScroll = 0;
                    }

                    if (appScroll > maxScroll) {
                        appScroll = maxScroll;
                    }

                    lastY = y;

                    invalidate();
                }

                return true;
            }

            if (
                    event.getAction()
                            ==
                    MotionEvent.ACTION_UP
            ) {

                if (page == 0) {

                    if (
                            x >= 0 &&
                            x <= 330 &&
                            y >= 70 &&
                            y <= 415
                    ) {

                        int index =
                                (int)(
                                        (y - 81) / 53f
                                );

                        if (
                                index >= 0 &&
                                index < menu.length
                        ) {

                            if (index == selected) {
                                openSelected();
                            } else {
                                selected = index;
                                invalidate();
                            }

                            return true;
                        }
                    }

                } else {

                    if (
                            x > 1080 &&
                            y < 60
                    ) {

                        page = 0;
                        invalidate();

                        return true;
                    }

                    int columns = 5;

                    float cellWidth =
                            1280f / columns;

                    float adjustedY =
                            y - 70f + appScroll;

                    if (adjustedY >= 0) {

                        int column =
                                (int)(
                                        x / cellWidth
                                );

                        int row =
                                (int)(
                                        adjustedY / 105f
                                );

                        int index =
                                row * columns + column;

                        if (
                                index >= 0 &&
                                index < apps.size()
                        ) {

                            try {
                                startActivity(
                                        apps.get(index).intent
                                );
                            } catch (Exception e) {
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
