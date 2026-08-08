package ro.f01.id6launcher;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        setContentView(new LauncherView());
    }

    private void launchPackage(String packageName, Intent fallback) {
        try {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packageName);

            if (intent != null) {
                startActivity(intent);
                return;
            }
        } catch (Exception ignored) {
        }

        try {
            startActivity(fallback);
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Aplicatia nu este instalata",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void openNavigation() {
        Intent fallback = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=Bucharest")
        );

        launchPackage("com.waze", fallback);
    }

    private void openMedia() {
        Intent fallback = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://open.spotify.com")
        );

        launchPackage("com.spotify.music", fallback);
    }

    private void openPhone() {
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

    private void openSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        } catch (Exception ignored) {
        }
    }

    private void openApps() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
            startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    private class LauncherView extends View {

        private Bitmap background;
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        LauncherView() {
            super(MainActivity.this);

            BitmapDrawable drawable =
                    (BitmapDrawable) getResources().getDrawable(
                            R.drawable.home_screen
                    );

            background = drawable.getBitmap();
            setFocusable(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            Rect source = new Rect(
                    0,
                    0,
                    background.getWidth(),
                    background.getHeight()
            );

            Rect destination = new Rect(
                    0,
                    0,
                    getWidth(),
                    getHeight()
            );

            canvas.drawBitmap(
                    background,
                    source,
                    destination,
                    paint
            );
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if (event.getAction() != MotionEvent.ACTION_UP) {
                return true;
            }

            float x = event.getX() * 1920f / getWidth();
            float y = event.getY() * 720f / getHeight();

            // Apps - iconita sus stanga
            if (x >= 165 && x <= 290 &&
                y >= 0 && y <= 90) {

                openApps();
                return true;
            }

            // Zona principala
            if (y >= 90 && y <= 650) {

                // Navigation
                if (x < 315) {
                    openNavigation();
                    return true;
                }

                // My Vehicle
                if (x < 980) {
                    Toast.makeText(
                            MainActivity.this,
                            "BMW F01 2011 pre-LCI • DJ 10 SDS",
                            Toast.LENGTH_SHORT
                    ).show();

                    return true;
                }

                // Media
                if (x < 1285) {
                    openMedia();
                    return true;
                }

                // Communication
                if (x < 1605) {
                    openPhone();
                    return true;
                }

                // Settings
                openSettings();
                return true;
            }

            return true;
        }
    }
}
