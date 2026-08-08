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

    LauncherView view;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideBars();
        view = new LauncherView(this);
        setContentView(view);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
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
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    void openNav() {
        openFirst(new String[]{"com.waze","com.google.android.apps.maps"},
                new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Bucharest")));
    }

    void openMedia() {
        openFirst(new String[]{"com.spotify.music","com.google.android.apps.youtube.music",
                        "com.maxmpz.audioplayer"},
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

    void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override public boolean dispatchKeyEvent(KeyEvent e) {
        if (view != null && view.handleKey(e)) return true;
        return super.dispatchKeyEvent(e);
    }

    @Override public boolean dispatchGenericMotionEvent(MotionEvent e) {
        if (view != null && view.handleMotion(e)) return true;
        return super.dispatchGenericMotionEvent(e);
    }

    class AppEntry {
        String label;
        Drawable icon;
        Intent intent;
    }

    class LauncherView extends View {
        final float BW = 1280f, BH = 480f;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Handler handler = new Handler();

        String[] menu = {
                "Multimedia","Radio","Telephone","Navigation",
                "ConnectedDrive","Vehicle Info","Settings","Apps"
        };

        int page = 0; // 0 NBT, 1 Apps, 2 Diagnostic
        int selected = 0;
        String keyDiag = "Astept comanda iDrive...";
        String motionDiag = "Niciun MotionEvent detectat";

        ArrayList<AppEntry> apps = new ArrayList<>();
        float downX, downY, lastY, appScroll = 0f;
        boolean vertical = false;

        Runnable tick = new Runnable() {
            @Override public void run() {
                invalidate();
                handler.postDelayed(this, 1000);
            }
        };

        LauncherView(Context c) {
            super(c);
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
            loadApps();
            handler.post(tick);
        }

        float sx(){ return getWidth()/BW; }
        float sy(){ return getHeight()/BH; }

        void fill(Canvas c,int color,float l,float t,float r,float b){
            p.setStyle(Paint.Style.FILL); p.setColor(color);
            c.drawRect(l*sx(),t*sy(),r*sx(),b*sy(),p);
        }

        void text(Canvas c,String s,float x,float y,float size,int color,Paint.Align a){
            p.setStyle(Paint.Style.FILL); p.setColor(color); p.setTextAlign(a);
            p.setTypeface(Typeface.create("sans",Typeface.NORMAL));
            p.setTextSize(size*sy());
            c.drawText(s,x*sx(),y*sy(),p);
        }

        @Override protected void onDraw(Canvas c) {
            if(page==0) drawNBT(c);
            else if(page==1) drawApps(c);
            else drawDiag(c);
        }

        void drawNBT(Canvas c) {
            c.drawColor(Color.rgb(4,5,6));
            fill(c,Color.rgb(15,17,19),0,0,1280,50);

            String tm = new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date());
            String dt = new SimpleDateFormat("dd.MM.yyyy",new Locale("ro","RO")).format(new Date());

            text(c,"BMW",24,32,17,Color.WHITE,Paint.Align.LEFT);
            text(c,dt,1080,31,13,Color.LTGRAY,Paint.Align.RIGHT);
            text(c,tm,1240,33,21,Color.WHITE,Paint.Align.RIGHT);

            float top=68,row=42;
            for(int i=0;i<menu.length;i++){
                float y=top+i*row;
                if(i==selected){
                    fill(c,Color.rgb(45,47,49),32,y-26,420,y+8);
                    fill(c,Color.rgb(235,127,25),32,y-26,37,y+8);
                }
                text(c,menu[i],58,y,18,
                        i==selected?Color.WHITE:Color.rgb(185,188,190),
                        Paint.Align.LEFT);
            }

            fill(c,Color.rgb(12,14,16),460,68,1250,408);
            fill(c,Color.rgb(52,54,56),460,68,1250,71);

            String title=menu[selected];
            text(c,title.toUpperCase(Locale.US),495,108,15,Color.LTGRAY,Paint.Align.LEFT);

            if(selected==0 || selected==1){
                text(c,"Media / Radio",495,158,25,Color.WHITE,Paint.Align.LEFT);
                text(c,"Player Android",495,190,15,Color.GRAY,Paint.Align.LEFT);
            } else if(selected==2){
                text(c,"Telephone",495,158,25,Color.WHITE,Paint.Align.LEFT);
                text(c,"Bluetooth / Dialer",495,190,15,Color.GRAY,Paint.Align.LEFT);
            } else if(selected==3){
                text(c,"Navigation",495,158,25,Color.WHITE,Paint.Align.LEFT);
                text(c,"Waze / Google Maps",495,190,15,Color.GRAY,Paint.Align.LEFT);
            } else if(selected==5){
                text(c,"BMW 7 Series",495,155,25,Color.WHITE,Paint.Align.LEFT);
                text(c,"F01 2011 pre-LCI",495,188,16,Color.LTGRAY,Paint.Align.LEFT);
                text(c,"DJ 10 SDS",495,220,18,Color.WHITE,Paint.Align.LEFT);
            } else if(selected==6){
                text(c,"Android Settings",495,158,25,Color.WHITE,Paint.Align.LEFT);
            } else if(selected==7){
                text(c,"Installed applications",495,158,25,Color.WHITE,Paint.Align.LEFT);
                text(c,"Apasa iDrive pentru lista",495,190,15,Color.GRAY,Paint.Align.LEFT);
            } else {
                text(c,"ConnectedDrive",495,158,25,Color.WHITE,Paint.Align.LEFT);
                text(c,"CAN integration in lucru",495,190,15,Color.GRAY,Paint.Align.LEFT);
            }

            fill(c,Color.rgb(15,17,19),0,430,1280,480);
            text(c,"iDrive: rotire = selectie • apasare = OK • BACK = inapoi",
                    24,460,12,Color.rgb(165,168,170),Paint.Align.LEFT);
            text(c,"DIAG",1238,460,12,Color.rgb(235,127,25),Paint.Align.RIGHT);
        }

        void openSelected(){
            switch(selected){
                case 0:
                case 1: openMedia(); break;
                case 2: openPhone(); break;
                case 3: openNav(); break;
                case 5: toast("BMW F01 2011 pre-LCI • DJ 10 SDS"); break;
                case 6: openSettings(); break;
                case 7: page=1; invalidate(); break;
                default: toast("Functie in curs de integrare");
            }
        }

        void move(int d){
            selected+=d;
            if(selected<0) selected=menu.length-1;
            if(selected>=menu.length) selected=0;
            invalidate();
        }

        boolean handleKey(KeyEvent e){
            String act=e.getAction()==KeyEvent.ACTION_DOWN?"DOWN":"UP";
            keyDiag="KEY "+act+" code="+e.getKeyCode()+" "
                    +KeyEvent.keyCodeToString(e.getKeyCode())
                    +" scan="+e.getScanCode()+" repeat="+e.getRepeatCount();
            invalidate();

            if(e.getAction()!=KeyEvent.ACTION_DOWN) return false;
            int k=e.getKeyCode();

            if(k==KeyEvent.KEYCODE_DPAD_UP || k==KeyEvent.KEYCODE_DPAD_LEFT ||
                    k==KeyEvent.KEYCODE_MEDIA_PREVIOUS){
                if(page==0) move(-1);
                return true;
            }

            if(k==KeyEvent.KEYCODE_DPAD_DOWN || k==KeyEvent.KEYCODE_DPAD_RIGHT ||
                    k==KeyEvent.KEYCODE_MEDIA_NEXT){
                if(page==0) move(1);
                return true;
            }

            if(k==KeyEvent.KEYCODE_DPAD_CENTER || k==KeyEvent.KEYCODE_ENTER){
                if(page==0) openSelected();
                return true;
            }

            if(k==KeyEvent.KEYCODE_BACK || k==KeyEvent.KEYCODE_ESCAPE){
                if(page!=0){ page=0; invalidate(); return true; }
            }

            return false;
        }

        boolean handleMotion(MotionEvent e){
            float v=e.getAxisValue(MotionEvent.AXIS_VSCROLL);
            float h=e.getAxisValue(MotionEvent.AXIS_HSCROLL);
            float s=e.getAxisValue(MotionEvent.AXIS_SCROLL);

            motionDiag="src="+e.getSource()+" action="+e.getAction()
                    +" v="+v+" h="+h+" scroll="+s;
            invalidate();

            float val=Math.abs(v)>0.01f?v:(Math.abs(h)>0.01f?h:s);
            if(page==0 && Math.abs(val)>0.01f){
                if(val>0) move(-1); else move(1);
                return true;
            }
            return false;
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
                        ri.activityInfo.packageName,ri.activityInfo.name));
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
            d.setBounds(0,0,w,h); d.draw(c); return b;
        }

        void drawApps(Canvas c){
            c.drawColor(Color.rgb(5,6,7));
            fill(c,Color.rgb(17,19,21),0,0,1280,50);
            text(c,"APPS",24,33,18,Color.WHITE,Paint.Align.LEFT);
            text(c,"‹ HOME",1240,33,13,Color.LTGRAY,Paint.Align.RIGHT);

            int cols=6;
            float cellW=1280f/cols, cellH=92f, top=62f-appScroll;

            for(int i=0;i<apps.size();i++){
                int row=i/cols,col=i%cols;
                float l=col*cellW+9, y=top+row*cellH, r=(col+1)*cellW-9, b=y+80;
                if(b<50 || y>480) continue;

                fill(c,Color.rgb(18,20,22),l,y,r,b);

                try{
                    Bitmap icon=iconBitmap(apps.get(i).icon,38,38);
                    Rect src=new Rect(0,0,icon.getWidth(),icon.getHeight());
                    RectF dst=new RectF((l+10)*sx(),(y+18)*sy(),
                            (l+48)*sx(),(y+56)*sy());
                    c.drawBitmap(icon,src,dst,p);
                }catch(Exception ignored){}

                String label=apps.get(i).label;
                if(label.length()>15) label=label.substring(0,14)+"…";
                text(c,label,l+58,y+43,12,Color.WHITE,Paint.Align.LEFT);
            }
        }

        void drawDiag(Canvas c){
            c.drawColor(Color.rgb(4,5,6));
            fill(c,Color.rgb(18,20,22),0,0,1280,55);
            text(c,"iDrive INPUT DIAGNOSTIC",24,36,19,Color.WHITE,Paint.Align.LEFT);
            text(c,"‹ HOME",1240,36,13,Color.LTGRAY,Paint.Align.RIGHT);

            text(c,"Rotește, apasă, înclină și folosește BACK/MENU.",
                    26,92,14,Color.LTGRAY,Paint.Align.LEFT);

            fill(c,Color.rgb(20,22,24),24,115,1255,205);
            text(c,"ULTIMUL KEY EVENT",40,144,12,Color.rgb(235,127,25),Paint.Align.LEFT);
            text(c,keyDiag,40,180,15,Color.WHITE,Paint.Align.LEFT);

            fill(c,Color.rgb(20,22,24),24,225,1255,315);
            text(c,"ULTIMUL MOTION EVENT",40,254,12,Color.rgb(235,127,25),Paint.Align.LEFT);
            text(c,motionDiag,40,290,15,Color.WHITE,Paint.Align.LEFT);

            text(c,"Fă o poză după fiecare comandă iDrive.",
                    26,370,14,Color.LTGRAY,Paint.Align.LEFT);
            text(c,"Cu keyCode / scanCode mapăm controllerul 1:1.",
                    26,400,14,Color.LTGRAY,Paint.Align.LEFT);
        }

        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX()/sx(), y=e.getY()/sy();

            if(e.getAction()==MotionEvent.ACTION_DOWN){
                downX=x; downY=y; lastY=y; vertical=false; return true;
            }

            if(e.getAction()==MotionEvent.ACTION_MOVE){
                float dx=x-downX, dy=y-downY;
                if(page==1 && Math.abs(dy)>Math.abs(dx) && Math.abs(dy)>8){
                    vertical=true;
                    appScroll += lastY-y;
                    float content=((apps.size()+5)/6)*92f;
                    float max=Math.max(0,content-400f);
                    if(appScroll<0) appScroll=0;
                    if(appScroll>max) appScroll=max;
                    lastY=y; invalidate();
                }
                return true;
            }

            if(e.getAction()==MotionEvent.ACTION_UP){
                float dx=x-downX, dy=y-downY;

                if(!vertical && Math.abs(dx)>120 && Math.abs(dx)>Math.abs(dy)){
                    if(dx<0){
                        if(page==0) page=1;
                        else if(page==1) page=2;
                    }else{
                        if(page==2) page=1;
                        else if(page==1) page=0;
                    }
                    invalidate(); return true;
                }

                if(page==0){
                    if(x>=30 && x<=430 && y>=40 && y<=410){
                        int idx=(int)((y-42)/42f);
                        if(idx>=0 && idx<menu.length){
                            if(idx==selected) openSelected();
                            else { selected=idx; invalidate(); }
                            return true;
                        }
                    }
                    if(x>1110 && y>420){ page=2; invalidate(); return true; }
                }else if(page==1){
                    if(x>1080 && y<60){ page=0; invalidate(); return true; }

                    int cols=6;
                    float cellW=1280f/cols;
                    float ay=y-62f+appScroll;
                    if(ay>=0){
                        int col=(int)(x/cellW);
                        int row=(int)(ay/92f);
                        int idx=row*cols+col;
                        if(idx>=0 && idx<apps.size()){
                            try{ startActivity(apps.get(idx).intent); }
                            catch(Exception ex){ toast("Nu pot porni aplicatia"); }
                            return true;
                        }
                    }
                }else{
                    if(x>1080 && y<60){ page=0; invalidate(); return true; }
                }
            }
            return true;
        }
    }
}
