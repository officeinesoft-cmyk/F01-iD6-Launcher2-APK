package ro.f01.id6launcher;

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.provider.Settings;
import android.view.*;
import android.widget.Toast;

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

        if (hasFocus) {
            hideSystemUi();
        }
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

                Intent intent =
                        pm.getLaunchIntentForPackage(pkg);

                if (intent != null) {

                    startActivity(intent);
                    return;
                }

            } catch (Exception ignored) {
            }
        }

        try {

            Intent geo = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=Bucharest")
            );

            startActivity(geo);

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

                Intent intent =
                        pm.getLaunchIntentForPackage(pkg);

                if (intent != null) {

                    startActivity(intent);
                    return;
                }

            } catch (Exception ignored) {
            }
        }

        try {

            Intent music =
                    new Intent("android.intent.action.MUSIC_PLAYER");

            startActivity(music);

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

            startActivity(
                    new Intent(Intent.ACTION_DIAL)
            );

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

            startActivity(
                    new Intent(Settings.ACTION_SETTINGS)
            );

        } catch (Exception ignored) {
        }
    }

    class AppEntry {

        String label;
        String packageName;

        Drawable icon;

        Intent intent;
    }

    class LauncherView extends View {

        Paint paint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        Bitmap home;

        ArrayList<AppEntry> apps =
                new ArrayList<>();

        int page = 0;

        float downX;
        float downY;
        float lastY;

        float appScroll = 0;

        boolean verticalScrolling = false;

        final float BASE_W = 1280f;
        final float BASE_H = 480f;

        LauncherView(Context context) {

            super(context);

            setFocusable(true);

            BitmapDrawable drawable =
                    (BitmapDrawable)
                            getResources().getDrawable(
                                    R.drawable.home_screen
                            );

            home =
                    drawable.getBitmap();

            loadApps();
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

                String pkg =
                        ri.activityInfo.packageName;

                if (
                        pkg.equals(
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

        float sx() {

            return getWidth() /
                    BASE_W;
        }

        float sy() {

            return getHeight() /
                    BASE_H;
        }

        @Override
        protected void onDraw(
                Canvas canvas
        ) {

            super.onDraw(canvas);

            if (page == 0) {

                drawHome(canvas);

            } else {

                drawApps(canvas);
            }
        }

        void drawHome(
                Canvas canvas
        ) {

            Rect source =
                    new Rect(
                            0,
                            0,
                            home.getWidth(),
                            home.getHeight()
                    );

            Rect destination =
                    new Rect(
                            0,
                            0,
                            getWidth(),
                            getHeight()
                    );

            canvas.drawBitmap(
                    home,
                    source,
                    destination,
                    paint
            );

            paint.setColor(
                    Color.WHITE
            );

            paint.setTextSize(
                    16 * sy()
            );

            paint.setTextAlign(
                    Paint.Align.RIGHT
            );

            canvas.drawText(
                    "APPS  ›",
                    1260 * sx(),
                    462 * sy(),
                    paint
            );
        }

        void drawApps(
                Canvas canvas
        ) {

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
                    15 * sy()
            );

            paint.setColor(
                    Color.LTGRAY
            );

            canvas.drawText(
                    "‹  HOME",
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

                    Rect iconSource =
                            new Rect(
                                    0,
                                    0,
                                    icon.getWidth(),
                                    icon.getHeight()
                            );

                    RectF iconDestination =
                            new RectF(
                                    (left + 18) * sx(),
                                    (itemTop + 17) * sy(),
                                    (left + 72) * sx(),
                                    (itemTop + 71) * sy()
                            );

                    canvas.drawBitmap(
                            icon,
                            iconSource,
                            iconDestination,
                            paint
                    );

                } catch (Exception ignored) {
                }

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

        Bitmap drawableToBitmap(
                Drawable drawable,
                int width,
                int height
        ) {

            if (
                    drawable
                            instanceof BitmapDrawable
            ) {

                Bitmap bitmap =
                        ((BitmapDrawable)
                                drawable)
                                .getBitmap();

                return Bitmap
                        .createScaledBitmap(
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

            drawable.draw(
                    canvas
            );

            return bitmap;
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

                    if (
                            appScroll < 0
                    ) {

                        appScroll = 0;
                    }

                    if (
                            appScroll >
                            maxScroll
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
                    event.getAction()
                            ==
                    MotionEvent.ACTION_UP
            ) {

                float dx =
                        x - downX;

                float dy =
                        y - downY;

                if (
                        !verticalScrolling
                        &&
                        Math.abs(dx)
                                > 130
                        &&
                        Math.abs(dx)
                                >
                        Math.abs(dy)
                ) {

                    if (
                            dx < 0
                            &&
                            page == 0
                    ) {

                        page = 1;
                    }

                    else if (
                            dx > 0
                            &&
                            page == 1
                    ) {

                        page = 0;
                    }

                    invalidate();

                    return true;
                }

                if (
                        page == 0
                ) {

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
                            x > 1120
                            &&
                            y > 420
                    ) {

                        page = 1;

                        invalidate();

                        return true;
                    }

                } else {

                    if (
                            x > 1080
                            &&
                            y < 65
                    ) {

                        page = 0;

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
                                index
                                <
                                apps.size()
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
                }
            }

            return true;
        }
    }
}
