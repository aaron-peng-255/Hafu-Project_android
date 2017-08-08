package com.hafu.Hafu.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.hafu.Hafu.R;
import com.hafu.Hafu.view.CircleImageView;

import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity {

    private LayoutInflater from;
    private View totalView;
    private TextView mTextMessage;
    private SharedPreferences sp;
    private LinearLayout linearLayout;
    private ListView order_list;

    private CircleImageView ivHead;
    private RelativeLayout layout_choose;
    private RelativeLayout layout_photo;
    private RelativeLayout layout_close;

    private LinearLayout layout_all;
    protected int mScreenWidth;

    /**
     * 定义三种状态
     */
    private static final int REQUESTCODE_PIC = 1;//相册
    private static final int REQUESTCODE_CAM = 2;//相机
    private static final int REQUESTCODE_CUT = 3;//图片裁剪

    private Bitmap mBitmap;
    private File mFile;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setVisibility(View.VISIBLE);
                    mTextMessage.setText(R.string.title_home);
                    totalView.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setVisibility(View.GONE);
                    getCartView();
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setVisibility(View.GONE);
                    getProfileView();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //引入各分页布局文件
        linearLayout = (LinearLayout) findViewById(R.id.topContainer);
        from = LayoutInflater.from(MainActivity.this);
        totalView = from.inflate(R.layout.main_profile,linearLayout);
        totalView.setVisibility(View.GONE);

        mTextMessage = (TextView) findViewById(R.id.message);

        // 获取缓存内容
        sp = getSharedPreferences("msg",MODE_PRIVATE);
        String user = sp.getString("username","");
        String isLogin = sp.getString("isLogin","");
        mTextMessage.setText(user+isLogin);


        // 引入底部导航栏
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


    /**
     * 获取个人详情页面
     */
    private void getProfileView() {
        linearLayout.removeAllViews();
        totalView.setVisibility(View.VISIBLE);
        if (sp.getString("isLogin","").equals("TRUE")) {
            totalView = from.inflate(R.layout.main_profile,linearLayout);

            ivHead = (CircleImageView) totalView.findViewById(R.id.userIcon);
            layout_all = (LinearLayout) totalView.findViewById(R.id.layout_all);

            ivHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()){
                        case R.id.userIcon:
                            showMyDialog();
                            break;
                    }
                }
            });
        } else if (sp.getString("isLogin","").equals("FALSE")) {
            totalView = from.inflate(R.layout.waiting_for_login,linearLayout);
        } else {
            totalView.setVisibility(View.GONE);
            mTextMessage.setVisibility(View.VISIBLE);
            mTextMessage.setText("isLogin错误");
        }
    }

    /**
     * 获取订单界面
     */
    private void getCartView() {
        linearLayout.removeAllViews();
        totalView.setVisibility(View.VISIBLE);
        if (sp.getString("isLogin","").equals("TRUE")) {
            totalView = from.inflate(R.layout.order_list,linearLayout);
            Log.i("info","==>已登录，查看购物车");
        } else if (sp.getString("isLogin","").equals("FALSE")) {
            totalView = from.inflate(R.layout.warning_to_login,linearLayout);
            Log.i("warning","==>未登录，不可查看购物车");
            return;
        } else {
            totalView.setVisibility(View.GONE);
            mTextMessage.setVisibility(View.VISIBLE);
            mTextMessage.setText("isLogin错误");
            Log.i("error","==>缓存isLogin部分错误");
            return;
        }

        List<Map<String,Object>> lists = new ArrayList<>();
        int[] imgIDs = {R.drawable.star,R.drawable.pizza,R.drawable.ha,R.drawable.star,R.drawable.pizza,R.drawable.ha};
        String[] titles = {"星巴克","必胜客","哈根达斯","星巴克","必胜客","哈根达斯"};
        String[] dates = {"2017-08-10 15:33","2017-07-03 04:26","2017-06-29 20:01","2017-08-10 15:33","2017-07-03 04:26","2017-06-29 20:01"};
        int[] orderStatus = {0,1,2,0,1,2};

        for(int i = 0; i < imgIDs.length; i++) {
            Map<String,Object> map = new HashMap<>();
            map.put("img",imgIDs[i]);
            map.put("title",titles[i]);
            map.put("date",dates[i]);
            if (orderStatus[i] == 0)
                map.put("status","订单已完成");
            else if (orderStatus[i] == 1)
                map.put("status","订单已取消");
            else if (orderStatus[i] == 2)
                map.put("status","订单待付款");
            map.put("good_img",imgIDs[i]);
            map.put("good_details",titles[i] + " 等共" + Integer.toString(orderStatus[i] + 1) +"件");
            map.put("good_price","¥"+Integer.toString(orderStatus[i]*10 + 11));
            lists.add(map);
        }

        order_list = totalView.findViewById(R.id.order_list);
        String[] key = {"img","title","date","status","good_img","good_details","good_price"};
        int[] ids = {R.id.item_img,R.id.item_title,R.id.item_time,R.id.item_status,R.id.item_good_img,R.id.item_good_details,R.id.item_good_price};
        SimpleAdapter simpleAdapter = new SimpleAdapter(totalView.getContext(),lists,R.layout.order_list_item,key,ids);
        order_list.setAdapter(simpleAdapter);

    }

    /**
     * 注销动作
     * @param view
     */
    public void logout(View view) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("isLogin","FALSE");
        editor.putString("username","");
        editor.commit();
        linearLayout.removeAllViews();
        getProfileView();
    }

    /**
     * 登录动作
     * @param view
     */
    public void login(View view) {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
    }

    // 头像选择部分
    PopupWindow avatorPop;
    private void showMyDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.pop_show_dialog,
                null);
        layout_choose = (RelativeLayout) view.findViewById(R.id.layout_choose);
        layout_photo = (RelativeLayout) view.findViewById(R.id.layout_photo);
        layout_close = (RelativeLayout) view.findViewById(R.id.layout_close);

        layout_choose.setBackgroundColor(getResources().getColor(
                R.color.base_color_text_white));
        layout_photo.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.pop_bg_press));
        layout_close.setBackgroundColor(getResources().getColor(
                R.color.base_color_text_white));


        layout_photo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                layout_choose.setBackgroundColor(getResources().getColor(
                        R.color.base_color_text_white));
                layout_photo.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.pop_bg_press));
                layout_close.setBackgroundColor(getResources().getColor(
                        R.color.base_color_text_white));


                openCamera();

                // Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                //startActivityForResult(intent,);
            }
        });

        layout_choose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                layout_photo.setBackgroundColor(getResources().getColor(
                        R.color.base_color_text_white));
                layout_choose.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.pop_bg_press));
                layout_close.setBackgroundColor(getResources().getColor(
                        R.color.base_color_text_white));
                openPic();

            }
        });

        layout_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_photo.setBackgroundColor(getResources().getColor(
                        R.color.base_color_text_white));
                layout_close.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.pop_bg_press));
                layout_choose.setBackgroundColor(getResources().getColor(
                        R.color.base_color_text_white));
                avatorPop.dismiss();
            }
        });



        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels;
        avatorPop = new PopupWindow(view, mScreenWidth, 200);
        avatorPop.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    avatorPop.dismiss();
                    return true;
                }
                return false;
            }
        });

        avatorPop.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        avatorPop.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        avatorPop.setTouchable(true);
        avatorPop.setFocusable(true);
        avatorPop.setOutsideTouchable(true);
        avatorPop.setBackgroundDrawable(new BitmapDrawable());
        // 动画效果 从底部弹起
        avatorPop.setAnimationStyle(R.style.Animations_GrowFromBottom);
        avatorPop.showAtLocation(layout_all, Gravity.BOTTOM, 0, 0);
    }

    /**
     * 打开相册
     */
    private void openPic() {
        Intent picIntent = new Intent(Intent.ACTION_PICK,null);
        picIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        startActivityForResult(picIntent,REQUESTCODE_PIC);
    }

    /**
     * 调用相机
     */
    private void openCamera() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (!file.exists()){
                file.mkdirs();
            }
            mFile = new File(file, System.currentTimeMillis() + ".jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
            startActivityForResult(intent,REQUESTCODE_CAM);
        } else {
            Toast.makeText(this, "请确认已经插入SD卡", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUESTCODE_CAM:
                    startPhotoZoom(Uri.fromFile(mFile));
                    break;
                case REQUESTCODE_PIC:

                    if (data == null || data.getData() == null){
                        return;
                    }
                    startPhotoZoom(data.getData());

                    break;
                case REQUESTCODE_CUT:

                    if (data!= null){
                        setPicToView(data);
                    }
                    break;
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setPicToView(Intent data) {
        Bundle bundle =  data.getExtras();
        if (bundle != null){
            //这里也可以做文件上传
            mBitmap = bundle.getParcelable("data");
            ivHead.setImageBitmap(mBitmap);
        }
    }

    /**
     * 打开系统图片裁剪功能
     * @param uri
     */
    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");
        intent.putExtra("crop",true);
        intent.putExtra("aspectX",1);
        intent.putExtra("aspectY",1);
        intent.putExtra("outputX",300);
        intent.putExtra("outputY",300);
        intent.putExtra("scale",true); //黑边
        intent.putExtra("scaleUpIfNeeded",true); //黑边
        intent.putExtra("return-data",true);
        intent.putExtra("noFaceDetection",true);
        startActivityForResult(intent,REQUESTCODE_CUT);

    }

}