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

    LauncherView launcher;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        hideSystemUi();

        launcher = new LauncherView(this);
        setContentView(launcher);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    void openNavigation() {
        PackageManager pm = getPackageManager();

        String[] packages = {
                "com.waze",
                "com.google.android.apps.maps"
        };

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
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=Bucharest")
            ));
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Nu am gasit aplicatia de navigatie",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    void openMedia() {
        PackageManager pm = getPackageManager();

        String[] packages = {
                "com.spotify.music",
                "com.google.android.apps.youtube.music",
                "com.maxmpz.audioplayer"
        };

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
            startActivity(new Intent("android.intent.action.MUSIC_PLAYER"));
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Nu am gasit playerul audio",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    void openPhone() {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL));
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Telefon indisponibil",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    void openSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        } catch (Exception ignored) {}
    }

    class AppEntry {
        String label;
        String packageName;
        Drawable icon;
        Intent intent;
    }

    class DiagnosticEntry {
        String line1;
        String line2;

        DiagnosticEntry(String a, String b) {
            line1 = a;
            line2 = b;
        }
    }

    class LauncherView extends View {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap home;

        final float BASE_W = 1280f;
        final float BASE_H = 480f;

        // 0=HOME, 1=APPS, 2=DIAGNOSTIC
        int page = 0;

        ArrayList<AppEntry> apps = new ArrayList<>();
        ArrayList<DiagnosticEntry> diagnostics = new ArrayList<>();

        float downX, downY, lastY;
        float appScroll = 0f;
        float diagScroll = 0f;
        boolean verticalScrolling = false;

        Handler clockHandler = new Handler();

        Runnable clockTick = new Runnable() {
            @Override
            public void run() {
                invalidate();
                clockHandler.postDelayed(this, 1000);
            }
        };

        LauncherView(Context context) {
            super(context);

            BitmapDrawable drawable =
                    (BitmapDrawable) getResources().getDrawable(
                            R.drawable.home_screen
                    );

            home = drawable.getBitmap();

            setFocusable(true);

            loadApps();
            scanDiagnosticPackages();

            clockHandler.post(clockTick);
        }

        float sx() {
            return getWidth() / BASE_W;
        }

        float sy() {
            return getHeight() / BASE_H;
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            clockHandler.removeCallbacks(clockTick);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (page == 0) {
                drawHome(canvas);
            } else if (page == 1) {
                drawApps(canvas);
            } else {
                drawDiagnostics(canvas);
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

            // OVERLAY REAL: ora si data din Android
            Date now = new Date();

            SimpleDateFormat timeFormat =
                    new SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                    );

            SimpleDateFormat dateFormat =
                    new SimpleDateFormat(
                            "EEE, dd.MM.yyyy",
                            new Locale("ro", "RO")
                    );

            String time = timeFormat.format(now);
            String date = dateFormat.format(now);

            // fundal discret peste textul static din imagine
            paint.setColor(Color.argb(215, 8, 10, 12));
            canvas.drawRoundRect(
                    515 * sx(),
                    4 * sy(),
                    805 * sx(),
                    55 * sy(),
                    8 * sx(),
                    8 * sy(),
                    paint
            );

            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(Typeface.create("sans", Typeface.NORMAL));

            paint.setTextSize(27 * sy());
            canvas.drawText(
                    time,
                    590 * sx(),
                    37 * sy(),
                    paint
            );

            paint.setTextSize(15 * sy());
            paint.setColor(Color.LTGRAY);

            canvas.drawText(
                    date,
                    710 * sx(),
                    35 * sy(),
                    paint
            );

            // CAN/CLIMA: nu inventam valori.
            // Afisam doar starea de integrare pana identificam serviciul MCU.
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(11 * sy());
            paint.setColor(Color.rgb(180, 185, 188));

            canvas.drawText(
                    "CAN: TsBMW / YX-BMW-GD-2 • diagnostic activ",
                    22 * sx(),
                    466 * sy(),
                    paint
            );

            paint.setTextAlign(Paint.Align.RIGHT);
            paint.setTextSize(14 * sy());
            paint.setColor(Color.WHITE);

            canvas.drawText(
                    "APPS  ›",
                    1255 * sx(),
                    465 * sy(),
                    paint
            );
        }

        void loadApps() {

            apps.clear();

            PackageManager pm = getPackageManager();

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

                String pkg =
                        ri.activityInfo.packageName;

                if (pkg.equals(getPackageName())) {
                    continue;
                }

                AppEntry entry =
                        new AppEntry();

                entry.label =
                        ri.loadLabel(pm)
                                .toString();

                entry.packageName =
                        pkg;

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
                                pkg,
                                ri.activityInfo.name
                        )
                );

                entry.intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                apps.add(entry);
            }
        }

        void scanDiagnosticPackages() {

            diagnostics.clear();

            PackageManager pm = getPackageManager();

            List<PackageInfo> packages;

            try {
                packages =
                        pm.getInstalledPackages(
                                PackageManager.GET_ACTIVITIES |
                                PackageManager.GET_SERVICES |
                                PackageManager.GET_RECEIVERS
                        );
            } catch (Exception e) {
                diagnostics.add(
                        new DiagnosticEntry(
                                "Diagnostic error",
                                e.toString()
                        )
                );
                return;
            }

            String[] keywords = {
                    "can",
                    "mcu",
                    "car",
                    "vehicle",
                    "auto",
                    "bmw",
                    "ts",
                    "yx",
                    "hmi",
                    "media",
                    "bluetooth",
                    "bt"
            };

            for (PackageInfo pi : packages) {

                String pkg =
                        pi.packageName == null
                                ? ""
                                : pi.packageName;

                String label = "";

                try {
                    ApplicationInfo ai =
                            pm.getApplicationInfo(
                                    pkg,
                                    0
                            );

                    label =
                            pm.getApplicationLabel(ai)
                                    .toString();

                } catch (Exception ignored) {}

                String haystack =
                        (pkg + " " + label)
                                .toLowerCase(Locale.US);

                boolean match = false;

                for (String keyword : keywords) {

                    if (haystack.contains(keyword)) {
                        match = true;
                        break;
                    }
                }

                if (!match) {
                    continue;
                }

                String version =
                        pi.versionName == null
                                ? ""
                                : pi.versionName;

                diagnostics.add(
                        new DiagnosticEntry(
                                label.length() == 0
                                        ? pkg
                                        : label,
                                pkg + "   v" + version
                        )
                );

                if (pi.services != null) {
                    for (ServiceInfo si : pi.services) {

                        String name =
                                si.name == null
                                        ? ""
                                        : si.name;

                        String lower =
                                name.toLowerCase(Locale.US);

                        if (
                                lower.contains("can") ||
                                lower.contains("mcu") ||
                                lower.contains("car") ||
                                lower.contains("vehicle") ||
                                lower.contains("bmw")
                        ) {
                            diagnostics.add(
                                    new DiagnosticEntry(
                                            "SERVICE",
                                            name
                                    )
                            );
                        }
                    }
                }

                if (pi.receivers != null) {
                    for (ActivityInfo ri : pi.receivers) {

                        String name =
                                ri.name == null
                                        ? ""
                                        : ri.name;

                        String lower =
                                name.toLowerCase(Locale.US);

                        if (
                                lower.contains("can") ||
                                lower.contains("mcu") ||
                                lower.contains("car") ||
                                lower.contains("vehicle") ||
                                lower.contains("bmw")
                        ) {
                            diagnostics.add(
                                    new DiagnosticEntry(
                                            "RECEIVER",
                                            name
                                    )
                            );
                        }
                    }
                }
            }

            if (diagnostics.size() == 0) {

                diagnostics.add(
                        new DiagnosticEntry(
                                "Nu am gasit candidati evidenti",
                                "TsBMW/YX poate folosi un serviciu cu nume generic."
                        )
                );
            }
        }

        Bitmap drawableToBitmap(
                Drawable drawable,
                int width,
                int height
        ) {

            if (drawable instanceof BitmapDrawable) {

                Bitmap bitmap =
                        ((BitmapDrawable) drawable)
                                .getBitmap();

                return Bitmap.createScaledBitmap(
                        bitmap,
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

            drawable.draw(canvas);

            return bitmap;
        }

        void drawApps(Canvas canvas) {

            canvas.drawColor(
                    Color.rgb(
                            9,
                            11,
                            13
                    )
            );

            paint.setColor(
                    Color.rgb(
                            23,
                            26,
                            28
                    )
            );

            canvas.drawRect(
                    0,
                    0,
                    getWidth(),
                    58 * sy(),
                    paint
            );

            paint.setTypeface(
                    Typeface.create(
                            "sans",
                            Typeface.NORMAL
                    )
            );

            paint.setTextAlign(
                    Paint.Align.LEFT
            );

            paint.setTextSize(
                    25 * sy()
            );

            paint.setColor(
                    Color.WHITE
            );

            canvas.drawText(
                    "APPS",
                    28 * sx(),
                    39 * sy(),
                    paint
            );

            paint.setTextAlign(
                    Paint.Align.RIGHT
            );

            paint.setTextSize(
                    14 * sy()
            );

            paint.setColor(
                    Color.LTGRAY
            );

            canvas.drawText(
                    "‹ HOME     DIAGNOSTIC ›",
                    1245 * sx(),
                    38 * sy(),
                    paint
            );

            int columns = 5;

            float cellWidth =
                    1280f / columns;

            float cellHeight =
                    118f;

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
                        column * cellWidth
                                + 12;

                float itemTop =
                        top +
                        row * cellHeight;

                float right =
                        (column + 1)
                                * cellWidth
                                - 12;

                float bottom =
                        itemTop
                                + cellHeight
                                - 10;

                if (
                        bottom < 60 ||
                        itemTop > 480
                ) {
                    continue;
                }

                paint.setColor(
                        Color.rgb(
                                22,
                                25,
                                27
                        )
                );

                canvas.drawRoundRect(
                        left * sx(),
                        itemTop * sy(),
                        right * sx(),
                        bottom * sy(),
                        10 * sx(),
                        10 * sy(),
                        paint
                );

                paint.setColor(
                        Color.rgb(
                                242,
                                139,
                                38
                        )
                );

                canvas.drawRect(
                        left * sx(),
                        itemTop * sy(),
                        right * sx(),
                        (itemTop + 3) * sy(),
                        paint
                );

                AppEntry app =
                        apps.get(i);

                try {

                    Bitmap icon =
                            drawableToBitmap(
                                    app.icon,
                                    54,
                                    54
                            );

                    Rect src =
                            new Rect(
                                    0,
                                    0,
                                    icon.getWidth(),
                                    icon.getHeight()
                            );

                    RectF dst =
                            new RectF(
                                    (left + 18) * sx(),
                                    (itemTop + 17) * sy(),
                                    (left + 72) * sx(),
                                    (itemTop + 71) * sy()
                            );

                    canvas.drawBitmap(
                            icon,
                            src,
                            dst,
                            paint
                    );

                } catch (Exception ignored) {}

                paint.setTextAlign(
                        Paint.Align.LEFT
                );

                paint.setTextSize(
                        15 * sy()
                );

                paint.setColor(
                        Color.WHITE
                );

                String label =
                        app.label;

                if (
                        label.length() > 19
                ) {
                    label =
                            label.substring(
                                    0,
                                    18
                            ) + "…";
                }

                canvas.drawText(
                        label,
                        (left + 86) * sx(),
                        (itemTop + 52) * sy(),
                        paint
                );

                paint.setTextSize(
                        10 * sy()
                );

                paint.setColor(
                        Color.GRAY
                );

                canvas.drawText(
                        app.packageName,
                        (left + 86) * sx(),
                        (itemTop + 72) * sy(),
                        paint
                );
            }
        }

        void drawDiagnostics(Canvas canvas) {

            canvas.drawColor(
                    Color.rgb(
                            7,
                            9,
                            11
                    )
            );

            paint.setColor(
                    Color.rgb(
                            24,
                            27,
                            29
                    )
            );

            canvas.drawRect(
                    0,
                    0,
                    getWidth(),
                    58 * sy(),
                    paint
            );

            paint.setTextAlign(
                    Paint.Align.LEFT
            );

            paint.setTypeface(
                    Typeface.create(
                            "sans",
                            Typeface.NORMAL
                    )
            );

            paint.setTextSize(
                    23 * sy()
            );

            paint.setColor(
                    Color.WHITE
            );

            canvas.drawText(
                    "CAN / MCU DIAGNOSTIC",
                    25 * sx(),
                    38 * sy(),
                    paint
            );

            paint.setTextAlign(
                    Paint.Align.RIGHT
            );

            paint.setTextSize(
                    13 * sy()
            );

            paint.setColor(
                    Color.LTGRAY
            );

            canvas.drawText(
                    "‹ APPS",
                    1245 * sx(),
                    38 * sy(),
                    paint
            );

            paint.setTextAlign(
                    Paint.Align.LEFT
            );

            paint.setTextSize(
                    12 * sy()
            );

            paint.setColor(
                    Color.rgb(
                            242,
                            139,
                            38
                    )
            );

            canvas.drawText(
                    "HMI BMW.G5.D.Q.F01 • MCU TsBMW.240709(W) • CAN YX-BMW-GD-2",
                    25 * sx(),
                    78 * sy(),
                    paint
            );

            float top =
                    100f - diagScroll;

            float rowH =
                    58f;

            for (
                    int i = 0;
                    i < diagnostics.size();
                    i++
            ) {

                float y =
                        top +
                        i * rowH;

                if (
                        y + rowH < 90 ||
                        y > 480
                ) {
                    continue;
                }

                paint.setColor(
                        Color.rgb(
                                20,
                                23,
                                25
                        )
                );

                canvas.drawRoundRect(
                        18 * sx(),
                        y * sy(),
                        1260 * sx(),
                        (y + 48) * sy(),
                        7 * sx(),
                        7 * sy(),
                        paint
                );

                paint.setTextAlign(
                        Paint.Align.LEFT
                );

                paint.setColor(
                        Color.WHITE
                );

                paint.setTextSize(
                        14 * sy()
                );

                canvas.drawText(
                        diagnostics.get(i).line1,
                        32 * sx(),
                        (y + 20) * sy(),
                        paint
                );

                paint.setColor(
                        Color.GRAY
                );

                paint.setTextSize(
                        11 * sy()
                );

                canvas.drawText(
                        diagnostics.get(i).line2,
                        32 * sx(),
                        (y + 39) * sy(),
                        paint
                );
            }
        }

        @Override
        public boolean onTouchEvent(
                MotionEvent event
        ) {

            float x =
                    event.getX() /
                            sx();

            float y =
                    event.getY() /
                            sy();

            if (
                    event.getAction()
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
                    event.getAction()
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
                            ((apps.size() + 4) / 5)
                                    * 118f;

                    float maxScroll =
                            Math.max(
                                    0,
                                    contentHeight
                                            - 390f
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

                if (
                        page == 2
                        &&
                        Math.abs(dy)
                                >
                        Math.abs(dx)
                        &&
                        Math.abs(dy) > 8
                ) {

                    verticalScrolling =
                            true;

                    diagScroll +=
                            lastY - y;

                    float contentHeight =
                            diagnostics.size()
                                    * 58f;

                    float maxScroll =
                            Math.max(
                                    0,
                                    contentHeight
                                            - 370f
                            );

                    if (diagScroll < 0) {
                        diagScroll = 0;
                    }

                    if (diagScroll > maxScroll) {
                        diagScroll = maxScroll;
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

                float dx =
                        x - downX;

                float dy =
                        y - downY;

                // SWIPE PAGINI
                if (
                        !verticalScrolling
                        &&
                        Math.abs(dx) > 130
                        &&
                        Math.abs(dx) > Math.abs(dy)
                ) {

                    if (dx < 0) {
                        if (page < 2) {
                            page++;
                        }
                    } else {
                        if (page > 0) {
                            page--;
                        }
                    }

                    invalidate();

                    return true;
                }

                if (page == 0) {

                    if (
                            x < 210
                            &&
                            y > 58
                            &&
                            y < 430
                    ) {
                        openNavigation();
                        return true;
                    }

                    if (
                            x >= 210
                            &&
                            x < 655
                            &&
                            y > 58
                            &&
                            y < 430
                    ) {

                        Toast.makeText(
                                MainActivity.this,
                                "BMW F01 2011 pre-LCI • DJ 10 SDS",
                                Toast.LENGTH_SHORT
                        ).show();

                        return true;
                    }

                    if (
                            x >= 655
                            &&
                            x < 860
                            &&
                            y > 58
                            &&
                            y < 430
                    ) {
                        openMedia();
                        return true;
                    }

                    if (
                            x >= 860
                            &&
                            x < 1070
                            &&
                            y > 58
                            &&
                            y < 430
                    ) {
                        openPhone();
                        return true;
                    }

                    if (
                            x >= 1070
                            &&
                            y > 58
                            &&
                            y < 430
                    ) {
                        openSettings();
                        return true;
                    }

                    if (
                            x > 1110
                            &&
                            y > 420
                    ) {
                        page = 1;
                        invalidate();
                        return true;
                    }

                } else if (page == 1) {

                    if (
                            x > 1080
                            &&
                            y < 65
                    ) {
                        page = 2;
                        invalidate();
                        return true;
                    }

                    int columns = 5;

                    float cellWidth =
                            1280f /
                            columns;

                    float adjustedY =
                            y
                            - 70f
                            + appScroll;

                    if (
                            adjustedY >= 0
                    ) {

                        int column =
                                (int)
                                        (x /
                                         cellWidth);

                        int row =
                                (int)
                                        (adjustedY /
                                         118f);

                        int index =
                                row
                                * columns
                                + column;

                        if (
                                index >= 0
                                &&
                                index < apps.size()
                        ) {

                            try {

                                startActivity(
                                        apps.get(index)
                                                .intent
                                );

                            } catch (Exception e) {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Nu pot porni aplicatia",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                            return true;
                        }
                    }

                } else {

                    if (
                            x > 1080
                            &&
                            y < 65
                    ) {
                        page = 1;
                        invalidate();
                        return true;
                    }
                }
            }

            return true;
        }
    }
}
