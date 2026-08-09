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

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideBars();
        view = new NbtView(this);
        setContentView(view);
    }

    @Override public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);
        if (f) hideBars();
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

    @Override public boolean dispatchKeyEvent(KeyEvent e) {
        if (view != null && view.handleKey(e)) return true;
        return super.dispatchKeyEvent(e);
    }

    void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    void openFirst(String[] pkgs, Intent fallback) {
        PackageManager pm = getPackageManager();
        for (String pkg : pkgs) {
            try {
                Intent i = pm.getLaunchIntentForPackage(pkg);
                if (i != null) { startActivity(i); return; }
            } catch (Exception ignored) {}
        }
        try { startActivity(fallback); }
        catch (Exception e) { toast("Aplicatia nu este disponibila"); }
    }

    void openNavigation() {
        openFirst(new String[]{"com.waze","com.google.android.apps.maps"},
                new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Bucharest")));
    }

    void openMedia() {
        openFirst(new String[]{"com.spotify.music","com.google.android.apps.youtube.music","com.maxmpz.audioplayer"},
                new Intent("android.intent.action.MUSIC_PLAYER"));
    }

    void openPhone() {
        try { startActivity(new Intent(Intent.ACTION_DIAL)); }
        catch (Exception e) { toast("Telefon indisponibil"); }
    }

    void openSettings() {
        try { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
        catch (Exception ignored) {}
    }

    class AppEntry {
        String label;
        Drawable icon;
        Intent intent;
    }

    class NbtView extends View {
        final float BW=1280f, BH=480f;
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap home;
        Handler handler=new Handler();

        String[] menu={
                "CD/Multimedia","Radio","Telephone","Navigation","Office",
                "ConnectedDrive","Vehicle information","Settings","Apps"
        };

        int selected=0;
        int page=0; // 0 Home, 1 Apps
        ArrayList<AppEntry> apps=new ArrayList<>();

        float appScroll=0f, downX, downY, lastY;
        boolean vertical=false;

        Runnable tick=new Runnable() {
            @Override public void run() {
                invalidate();
                handler.postDelayed(this,1000);
            }
        };

        NbtView(Context c) {
            super(c);
            BitmapDrawable d=(BitmapDrawable)getResources().getDrawable(R.drawable.nbt_home_v8);
            home=d.getBitmap();
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
            loadApps();
            handler.post(tick);
        }

        @Override protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            handler.removeCallbacks(tick);
        }

        float sx(){ return getWidth()/BW; }
        float sy(){ return getHeight()/BH; }

        void fill(Canvas c,int color,float l,float t,float r,float b){
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            c.drawRect(l*sx(),t*sy(),r*sx(),b*sy(),p);
        }

        void text(Canvas c,String s,float x,float y,float size,int color,Paint.Align a){
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setTextAlign(a);
            p.setTypeface(Typeface.create("sans",Typeface.NORMAL));
            p.setTextSize(size*sy());
            c.drawText(s,x*sx(),y*sy(),p);
        }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            if (page==0) drawHome(c); else drawApps(c);
        }

        void drawHome(Canvas c) {
            Rect src=new Rect(0,0,home.getWidth(),home.getHeight());
            Rect dst=new Rect(0,0,getWidth(),getHeight());
            c.drawBitmap(home,src,dst,p);

            // redraw only central menu so selection is live
            fill(c,Color.argb(238,8,9,10),430,93,840,475);

            // real time/date
            fill(c,Color.argb(235,8,9,10),855,15,1255,73);
            String time=new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date());
            String date=new SimpleDateFormat("dd.MM.yyyy",new Locale("ro","RO")).format(new Date());

            text(c,time,972,49,20,Color.WHITE,Paint.Align.RIGHT);
            text(c,date,1232,49,16,Color.WHITE,Paint.Align.RIGHT);

            float top=126f,rowH=39f;
            for(int i=0;i<menu.length;i++){
                float y=top+i*rowH;

                if(i==selected){
                    fill(c,Color.argb(225,40,25,12),440,y-27,820,y+7);
                    fill(c,Color.rgb(235,127,25),440,y-27,820,y-23);
                }

                text(c,menu[i],470,y,19,
                        i==selected?Color.WHITE:Color.rgb(215,215,215),
                        Paint.Align.LEFT);
            }

            fill(c,Color.argb(225,8,9,10),930,421,1270,478);
            text(c,"iDrive rotire = selectie",1240,451,11,Color.LTGRAY,Paint.Align.RIGHT);
            text(c,"Touch = OK",1240,469,11,Color.rgb(235,127,25),Paint.Align.RIGHT);
        }

        void moveSelection(int delta){
            selected+=delta;
            if(selected<0) selected=menu.length-1;
            if(selected>=menu.length) selected=0;
            invalidate();
        }

        void openSelected(){
            switch(selected){
                case 0:
                case 1: openMedia(); break;
                case 2: openPhone(); break;
                case 3: openNavigation(); break;
                case 4: toast("Office"); break;
                case 5: toast("ConnectedDrive - integrare CAN in lucru"); break;
                case 6: toast("BMW F01 2011 pre-LCI • DJ 10 SDS"); break;
                case 7: openSettings(); break;
                case 8: page=1; invalidate(); break;
            }
        }

        // HU diagnostic confirmed rotation arrives on KEY UP:
        // 88 = MEDIA_PREVIOUS, 87 = MEDIA_NEXT
        boolean handleKey(KeyEvent e){
            if(e.getAction()!=KeyEvent.ACTION_UP) return false;

            int code=e.getKeyCode();

            if(code==88 || code==KeyEvent.KEYCODE_MEDIA_PREVIOUS){
                if(page==0) moveSelection(-1);
                else scrollApps(-1);
                return true;
            }

            if(code==87 || code==KeyEvent.KEYCODE_MEDIA_NEXT){
                if(page==0) moveSelection(1);
                else scrollApps(1);
                return true;
            }

            if(code==KeyEvent.KEYCODE_BACK || code==KeyEvent.KEYCODE_ESCAPE){
                if(page!=0){
                    page=0;
                    invalidate();
                    return true;
                }
            }

            return false;
        }

        void scrollApps(int direction){
            appScroll += direction*44f;

            float content=((apps.size()+5)/6)*88f;
            float max=Math.max(0,content-400f);

            if(appScroll<0) appScroll=0;
            if(appScroll>max) appScroll=max;

            invalidate();
        }

        void loadApps(){
            apps.clear();
            PackageManager pm=getPackageManager();

            Intent q=new Intent(Intent.ACTION_MAIN,null);
            q.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> list=pm.queryIntentActivities(q,0);

            Collections.sort(list,new Comparator<ResolveInfo>(){
                @Override public int compare(ResolveInfo a,ResolveInfo b){
                    return a.loadLabel(pm).toString()
                            .compareToIgnoreCase(b.loadLabel(pm).toString());
                }
            });

            for(ResolveInfo ri:list){
                if(ri.activityInfo.packageName.equals(getPackageName())) continue;

                AppEntry a=new AppEntry();
                a.label=ri.loadLabel(pm).toString();
                a.icon=ri.loadIcon(pm);

                a.intent=new Intent(Intent.ACTION_MAIN);
                a.intent.addCategory(Intent.CATEGORY_LAUNCHER);
                a.intent.setComponent(new ComponentName(
                        ri.activityInfo.packageName,
                        ri.activityInfo.name));
                a.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                apps.add(a);
            }
        }

        Bitmap iconBitmap(Drawable d,int w,int h){
            if(d instanceof BitmapDrawable){
                return Bitmap.createScaledBitmap(((BitmapDrawable)d).getBitmap(),w,h,true);
            }

            Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
            Canvas c=new Canvas(b);
            d.setBounds(0,0,w,h);
            d.draw(c);
            return b;
        }

        void drawApps(Canvas c){
            c.drawColor(Color.rgb(5,6,7));

            fill(c,Color.rgb(17,19,21),0,0,1280,50);
            text(c,"APPS",24,33,17,Color.WHITE,Paint.Align.LEFT);
            text(c,"‹ HOME",1240,33,12,Color.LTGRAY,Paint.Align.RIGHT);

            int cols=6;
            float cw=1280f/cols;
            float ch=88f;
            float top=61f-appScroll;

            for(int i=0;i<apps.size();i++){
                int row=i/cols;
                int col=i%cols;

                float l=col*cw+9;
                float y=top+row*ch;
                float r=(col+1)*cw-9;
                float b=y+76;

                if(b<50 || y>480) continue;

                fill(c,Color.rgb(18,20,22),l,y,r,b);

                try{
                    Bitmap icon=iconBitmap(apps.get(i).icon,36,36);
                    Rect src=new Rect(0,0,icon.getWidth(),icon.getHeight());
                    RectF dst=new RectF(
                            (l+9)*sx(),
                            (y+17)*sy(),
                            (l+45)*sx(),
                            (y+53)*sy());
                    c.drawBitmap(icon,src,dst,p);
                }catch(Exception ignored){}

                String label=apps.get(i).label;
                if(label.length()>15) label=label.substring(0,14)+"…";

                text(c,label,l+55,y+41,11,Color.WHITE,Paint.Align.LEFT);
            }
        }

        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX()/sx();
            float y=e.getY()/sy();

            if(e.getAction()==MotionEvent.ACTION_DOWN){
                downX=x;
                downY=y;
                lastY=y;
                vertical=false;
                return true;
            }

            if(e.getAction()==MotionEvent.ACTION_MOVE){
                float dx=x-downX;
                float dy=y-downY;

                if(page==1 && Math.abs(dy)>Math.abs(dx) && Math.abs(dy)>8){
                    vertical=true;

                    appScroll += lastY-y;

                    float content=((apps.size()+5)/6)*88f;
                    float max=Math.max(0,content-400f);

                    if(appScroll<0) appScroll=0;
                    if(appScroll>max) appScroll=max;

                    lastY=y;
                    invalidate();
                }

                return true;
            }

            if(e.getAction()==MotionEvent.ACTION_UP){
                if(page==0){
                    if(x>=430 && x<=840 && y>=90 && y<=475){
                        int idx=(int)((y-99)/39f);

                        if(idx>=0 && idx<menu.length){
                            if(idx==selected) openSelected();
                            else{
                                selected=idx;
                                invalidate();
                            }
                            return true;
                        }
                    }
                }else{
                    if(x>1080 && y<60){
                        page=0;
                        invalidate();
                        return true;
                    }

                    int cols=6;
                    float cw=1280f/cols;
                    float ay=y-61f+appScroll;

                    if(ay>=0){
                        int col=(int)(x/cw);
                        int row=(int)(ay/88f);
                        int idx=row*cols+col;

                        if(idx>=0 && idx<apps.size()){
                            try{
                                startActivity(apps.get(idx).intent);
                            }catch(Exception ex){
                                toast("Nu pot porni aplicatia");
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
