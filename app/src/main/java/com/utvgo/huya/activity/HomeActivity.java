package com.utvgo.huya.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lzy.okgo.model.Response;
import com.utvgo.handsome.diff.DiffConfig;
import com.utvgo.handsome.diff.IPurchase;
import com.utvgo.handsome.diff.ITVBox;
import com.utvgo.handsome.diff.Platform;
import com.utvgo.handsome.interfaces.CommonCallback;
import com.utvgo.handsome.interfaces.JsonCallback;
import com.utvgo.handsome.utils.XLog;
import com.utvgo.handsome.views.CustomVideoView;
import com.utvgo.huya.BuildConfig;
import com.utvgo.huya.HuyaApplication;
import com.utvgo.huya.R;
import com.utvgo.huya.beans.BaseResponse;
import com.utvgo.huya.beans.BeanExitPage;
import com.utvgo.huya.beans.OpItem;
import com.utvgo.huya.beans.ProgramContent;
import com.utvgo.huya.beans.ProgramInfoBase;
import com.utvgo.huya.beans.TypesBean;
import com.utvgo.huya.listeners.MyDialogEnterListener;
import com.utvgo.huya.net.NetworkService;
import com.utvgo.huya.utils.HiFiDialogTools;
import com.utvgo.huya.utils.StringUtils;
import com.utvgo.huya.utils.ToastUtil;
import com.vod.VPlayer;
import com.vod.event.IPlayerEvent;
import com.vod.listener.PlayerEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends BuyActivity {

    @BindView(R.id.btn_tab_0)
    Button mainTabButton1;

    @BindView(R.id.vv_small)
    CustomVideoView videoView;

    @BindView(R.id.sv_video)
    SurfaceView svVideo;

    final int flContentIdArray[] = {R.id.bits_1, R.id.bits_2, R.id.bits_3, R.id.bits_4, R.id.bits_5,
            R.id.bits_6, R.id.bits_7, R.id.bits_8, R.id.bits_9, R.id.bits_10};
    final int flContentButtonIdArray[] = {R.id.btn_fl_1, R.id.btn_fl_2, R.id.btn_fl_3, R.id.btn_fl_4, R.id.btn_fl_5,
            R.id.btn_fl_6, R.id.btn_fl_7, R.id.btn_fl_8, R.id.btn_fl_9, R.id.btn_fl_10};
    ImageView flContentImageViewArray[] = new ImageView[flContentIdArray.length];

    //data
    List<OpItem> pageOpData = new ArrayList<>();
    OpItem videoData = null;

    TypesBean typesBean = new TypesBean();
    BeanExitPage beanExitPage;
    List<BeanExitPage.Data> endPushContentBean;

    int currentPlayingIndex = 0;
    String assetUrlArray[] = null;
    public VPlayer vvp;


    final View.OnClickListener itemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int index = -1;
            for (int i = 0; i < flContentImageViewArray.length; i++) {
                if (view.getId() == flContentImageViewArray[i].getId()) {
                    index = i;
                    break;
                }
            }
            if (index >= 0 && index < pageOpData.size()) {
                final OpItem bean = pageOpData.get(index);
                actionOnOp(bean);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(DiffConfig.CurrentPlatform==Platform.gzbn){
            if(platfromUtils.isFuMuLe2()){
                setContentView(R.layout.activity_gzfml2_home);;
            }else {
                setContentView(R.layout.activity_home);}
        }else {
            setContentView(R.layout.activity_home);
        }

        ButterKnife.bind(this);
        initView();
        stat("首页","");
        DiffConfig.CurrentPurchase.auth(this, new IPurchase.AuthCallback() {
            @Override
            public void onFinished(String message) {
                if(BuildConfig.DEBUG){
                    Toast.makeText(HomeActivity.this, "HomePage auth finished: " + (DiffConfig.CurrentPurchase.isPurchased() ? "is Purchased" : "not Purchased"), Toast.LENGTH_LONG).show();
                }
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        });
    }


    private void initView() {
        int i = 0;
        for (int id : flContentIdArray) {
            flContentImageViewArray[i] = findViewById(flContentIdArray[i]);
            flContentImageViewArray[i].setOnClickListener(itemOnClickListener);
            i++;
        }

        videoView = (CustomVideoView)findViewById(R.id.vv_small);

        mainTabButton1.requestFocus();
    }


    private View currentButton;

    @Override
    public void onClick(View v) {
        final Context context = this;
        final int viewId = v.getId();
        if(DiffConfig.CurrentPlatform == Platform.gzbn && platfromUtils.isFuMuLe2()){
            vvp.destroy();
        }
        switch (viewId) {
            case R.id.btn_main_order: {
                DiffConfig.CurrentPurchase.auth(this, new IPurchase.AuthCallback() {
                    @Override
                    public void onFinished(String message) {
                        if (DiffConfig.CurrentPurchase.isPurchased()) {
                            ToastUtil.show(context, "您已经是 " + getResources().getString(R.string.app_name) + " 尊贵会员");
                        } else {
                            if (!TextUtils.isEmpty(message)) {
                                ToastUtil.show(context, message);
                            }
                            DiffConfig.CurrentPurchase.pay(context, new CommonCallback() {
                                @Override
                                public void onFinished(Context context) {

                                }
                            });
                        }
                    }
                });
                break;
            }
            case R.id.btn_main_user_favor: {
                startActivity(new Intent(this, UserFavoriteActivity.class));
                break;
            }
            case R.id.btn_main_introduction: {
                if (DiffConfig.UseWebIntroduction) {
                    QWebViewActivity.navigateUrl(this, DiffConfig.IntroduceUrl, null);
                } else {
                    startActivity(new Intent(this, IntroduceActivity.class));
                }
                break;
            }
            case R.id.btn_main_user_center: {
                startActivity(new Intent(this, UserCenterActivity.class));
                break;
            }

            case R.id.btn_fl_video:
            {
                gotoMediaPlayer();
                break;
            }

            case R.id.btn_tab_1:
            {
                CategoryListActivity.show(context, 156);
                break;
            }

            case R.id.btn_tab_2:
            {
                CategoryListActivity.show(context, 157);
                break;
            }

            case R.id.btn_tab_3:
            {
                CategoryListActivity.show(context, 158);
                break;
            }

            default:
            {
                int index = Arrays.binarySearch(flContentButtonIdArray, viewId);
                if(viewId==R.id.btn_fl_10){index = 9;}
                if (index >= 0 && index < this.pageOpData.size()) {
                    final OpItem bean = this.pageOpData.get(index);
                    actionOnOp(bean);
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            turnHome();
        }
    }

    public void turnHome() {
        finish();
        if(DiffConfig.CurrentPlatform==Platform.gzbn){
            if (platfromUtils.isFML1()) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);}
        }else {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
//        android.os.Process.killProcess(android.os.Process.myPid());
//        System.exit(0);
    }

    @Override
    public void onBackPressed() {
        //endPushContentBean != null
        Log.d(TAG, "onBackPressed: "+HuyaApplication.hadBuy());
        if (!HuyaApplication.hadBuy()) {
            showQuitRecommend();
        } else {
            alertForQuit();
        }
    }

    @Override
    protected void onPause() {
        if (videoView != null) {
            videoView.pause();
        }else if(vvp != null){
            vvp.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (videoView != null) {
//            videoView.resume();
//        }else if(vvp!=null){
//           vvp.resume();
//        }
        if (!DiffConfig.validateDeviceKeyNO(this)) {
            return;
        }
        if (platfromUtils.isFuMuLe2()){
            svVideo.setVisibility(View.VISIBLE);
            fmlPlayVideo();
        }else {
            playVideo();
        }
    }
    @Override
    protected void onDestroy() {

        if(platfromUtils.isFuMuLe2()){
            vvp.destroy();
        }else{
            videoView.stopPlayback();
        }
        svVideo.setVisibility(View.GONE);
        super.onDestroy();
    }

    private void alertForQuit() {
        HiFiDialogTools.getInstance().showLeftRightTip(HomeActivity.this, "温馨提示", "确认退出" +
                getResources().getString(R.string.app_name), "确认", "取消", new MyDialogEnterListener() {
            @Override
            public void onClickEnter(Dialog dialog, Object object) {
                if (object instanceof Integer) {
                    if (((Integer) object) == 0) {
                        turnHome();
                    }
                }
            }
        });
    }

   private void showQuitRecommend() {
        Intent intent = new Intent(HomeActivity.this, ExitActivity.class);

        startActivityForResult(intent, ExitActivity.TAGRecommendExit);
    }


   private void showPageData(final String imagePrefix, final List<OpItem> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        this.pageOpData.clear();
        this.pageOpData.addAll(list);

        boolean hasVideo = false;
        for (int i = 0; i < list.size() && i < flContentImageViewArray.length; i++) {
            if (i < flContentImageViewArray.length) {
                OpItem bean = list.get(i);
                boolean isVideo = "1".equalsIgnoreCase(bean.getIsVideo());

                 if (i == 3) {
                        isVideo = true;
                  }

                if (isVideo) {
                    videoData = bean;
                    hasVideo = true;
                    String array[] = this.videoData.getVideoUrl().split(",");
                    if (array == null || array.length <= 1) {
                        array = this.videoData.getVideoUrl().split("\\|");
                    }
                    this.assetUrlArray = array;
                }

                if (!isVideo) {
                    ImageView imageView = flContentImageViewArray[i];
                    Glide.with(this).load(imagePrefix+bean.getImgUrl()).into(imageView);
                    //loadImage(imageView, bean.getImgUrl());
                }
            }
        }
        if (hasVideo) {

            if(DiffConfig.CurrentPlatform== Platform.gzbn){
                if (platfromUtils.isFuMuLe2()){
                    svVideo.setVisibility(View.VISIBLE);
                    fmlPlayVideo();
                }else {
                    setHahaPlayer(videoView);
                    playVideo();
                }

            }else {
                setHahaPlayer(videoView);
                playVideo();
            }
        }
    }

    @Override
    public void onDuration(long l) {
        super.onDuration(l);
//        if("gzbn".equals(DiffConfig.CurrentPlatform.name())){
//            if (platfromUtils.isFuMuLe2()) {
//                setHahaPlayerSize((int) getResources().getDimension(R.dimen.dp270), (int) getResources().getDimension(R.dimen.dp178),
//                        (int) getResources().getDimension(R.dimen.dp545), (int) getResources().getDimension(R.dimen.dp305));
//            }
//        }
    }


    public void hahaPlayEnd() {
        if (platfromUtils.isFuMuLe2()){
            svVideo.setVisibility(View.VISIBLE);
            fmlPlayVideo();
        }else {
            playVideo();
        }
    }
//二代
    public void fmlPlayVideo() {
        String fmlVodId="HuYaSeriesIDhuya0001test";
        if (this.assetUrlArray != null) {
            int index = new Random().nextInt(this.assetUrlArray.length);
            final String array[] = this.assetUrlArray;
            int playIndex = 0;
            if (index < 0 || index >= array.length) {
                playIndex = 0;
            }
            if (playIndex >= 0 && playIndex < array.length) {
                // playUrl(array[playIndex]);

                if( array[playIndex].startsWith("http")&&DiffConfig.CurrentPlatform==Platform.gzbn) {
                    array[playIndex] = "HuYaSeriesIDhuya0001test";
                }
                fmlVodId=array[playIndex];
            }
        }

        DiffConfig.CurrentTVBox.fetchUrlByVODAssetId(this, fmlVodId, new ITVBox.FetchUrlByVODAssetIdCallBack() {
            @Override
            public void onReceivedUrl(String vodId, String url) {


                vvp = new VPlayer(getBaseContext(), (int) getResources().getDimension(R.dimen.dp270), (int) getResources().getDimension(R.dimen.dp178), (int) getResources().getDimension(R.dimen.dp545), (int) getResources().getDimension(R.dimen.dp305));

                if (vvp != null && (vvp.getState() == 2 || vvp.getState() == 3)) {
                    vvp.stop();
                }

                vvp.play(url, 0);
                vvp.setPlayerEventListener(new PlayerEventListener() {
                    @Override
                    public void OnPlayerEvent(IPlayerEvent iPlayerEvent) {
                        Log.d("PlayGuizhouActivity", "OnPlayerEvent: type:" + iPlayerEvent.getType()
                                + "  reason:" + iPlayerEvent.getReason() + "  errorType:" + iPlayerEvent.getErrorType()
                                + "  errorReason:" + iPlayerEvent.getErrorReason());
                        if (iPlayerEvent.getType() == IPlayerEvent.TYPE_RTSP_PLAYER_PLAY_SUCCESS) {
                           //onDuration((long) vvp.getDuration() * 1000);
                           Log.d(TAG, "run: "+ vvp.getDuration());

                        }
                        if(iPlayerEvent.getType() == IPlayerEvent.TYPE_PLAYER_FRONT_STOP ||iPlayerEvent.getType() == IPlayerEvent.TYPE_PLAYER_END_OF_STREAM){
                            hahaPlayEnd();
                        }
                    }
                });
            }
        });

    }


    private void playVideo() {

        if (this.assetUrlArray != null) {
            int index = new Random().nextInt(this.assetUrlArray.length);
            final String array[] = this.assetUrlArray;
            int playIndex = index;
            if (index < 0 || index >= array.length) {
                playIndex = 0;
            }
            if (playIndex >= 0 && playIndex < array.length) {
               // playUrl(array[playIndex]);

                if( array[playIndex].startsWith("http")&&DiffConfig.CurrentPlatform==Platform.gzbn) {
                    array[playIndex] = "HuYaSeriesIDhuya0001test";
                }
                getHahaPlayerUrl(array[playIndex]);
            }
        }
    }

    void playUrl(final String assetUrl) {
        videoView.setVideoURI(Uri.parse(assetUrl));
    }

  private   void gotoMediaPlayer()
    {
        if(this.videoData != null)
        {
            String array[] = this.videoData.getHref().split(",");
            if (array == null || array.length <= 1) {
            array = this.videoData.getVideoUrl().split("\\|");
        }
            int index = currentPlayingIndex;
            if(index >= 0 && index < array.length)
            {
                String url = array[index];
                Uri uri = Uri.parse(url);
                String programId = uri.getQueryParameter("pkId");
                String channelId = uri.getQueryParameter("channelId");
                ArrayList<ProgramInfoBase> list = new ArrayList<>();
                ProgramInfoBase programInfoBase = new ProgramInfoBase();
                programInfoBase.setName("");
                programInfoBase.setChannelId(StringUtils.intValueOfString(channelId));
                programInfoBase.setPkId(StringUtils.intValueOfString(programId));
                programInfoBase.setMultiSetType("0");
                list.add(programInfoBase);
                PlayVideoActivity.play(this, list, 0, false);
            }
        }
    }

    /*
     **** network service
     */
    private void loadData() {
        NetworkService.defaultService().fetchHomePageData(this, new JsonCallback<BaseResponse<List<OpItem>>>() {
            @Override
            public void onSuccess(Response<BaseResponse<List<OpItem>>> response) {
                BaseResponse<List<OpItem>> bean = response.body();
                if (bean != null && bean.isOk()) {
                    showPageData(bean.getImageProfix(), bean.getData());
                } else {
                    HiFiDialogTools.getInstance().showLeftRightTip(HomeActivity.this, "温馨提示", "请求数据失败，请检查网络",
                            "重试", "取消", new MyDialogEnterListener() {
                                @Override
                                public void onClickEnter(Dialog dialog, Object object) {
                                    if (object instanceof Integer) {
                                        if (((Integer) object) == 0) {
                                            loadData();
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    private void loadTypesData() {
        NetworkService.defaultService().fetchHomePageNavData(this, new JsonCallback<TypesBean>() {
            @Override
            public void onSuccess(Response<TypesBean> response) {
                TypesBean bean = response.body();
                if (bean.isOk()) {
                    typesBean = bean;
                }
            }
        });
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(vvp != null){ ;
            vvp=null;
        }
        timeHandler.removeMessages(PLAY_TIME);
        svVideo.setVisibility(View.GONE);
    }

    public  void  checkIntentData(){

        String programId = getIntent().getStringExtra("pkId");
        String multisetType = getIntent().getStringExtra("multisetType");
        String channelId = getIntent().getStringExtra("channelId");

        String albumId = getIntent().getStringExtra("pkId");

        String topicId = getIntent().getStringExtra("themId");
        String styleId = getIntent().getStringExtra("styleId");
        Log.d(TAG, "checkIntentData:"+"programId"+programId+"multisetType"+multisetType+"channelId"+channelId+"albumId"+albumId+"topicId"+topicId+"styleId"+styleId);

        if(programId!=null && multisetType!=null && channelId!=null) {

            NetworkService.defaultService().fetchProgramContent(this, StringUtils.intValueOfString(programId),
                    multisetType, StringUtils.intValueOfString(channelId), new JsonCallback<BaseResponse<ProgramContent>>() {
                        @Override
                        public void onSuccess(Response<BaseResponse<ProgramContent>> response) {
                            BaseResponse<ProgramContent> data = response.body();
                            if (data.isOk()) {
                                ArrayList<ProgramInfoBase> list = new ArrayList<>();
                                list.add(data.getData());
                                PlayVideoActivity.play(getBaseContext(), list, 0, false);
                            }
                        }
                    });
        }
        if(albumId != null) {
            MediaAlbumActivity.show(this, StringUtils.intValueOfString(albumId));
        }
       if(topicId != null && styleId != null)
            TopicActivity.show(this, topicId, styleId);

    }
}