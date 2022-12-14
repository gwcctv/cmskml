package com.movtalent.app.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.listener.OnButtonClickListener;
import com.azhon.appupdate.listener.OnDownloadListener;
import com.azhon.appupdate.manager.DownloadManager;
import com.azhon.appupdate.utils.ScreenUtil;
import com.lib.common.util.SharePreferencesUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.CenterListPopupView;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.lib.common.util.DataInter;
import com.lib.common.util.utils.DataCleanManager;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.media.playerlib.PlayApp;
import com.movtalent.app.R;
import com.movtalent.app.adapter.setting.TextItemSection;
import com.movtalent.app.adapter.setting.TextItemSectionViewBinder;
import com.movtalent.app.model.dto.UpdateDto;
import com.movtalent.app.presenter.UpdatePresenter;
import com.movtalent.app.presenter.iview.IUpdate;
import com.movtalent.app.util.UserUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.drakeet.multitype.MultiTypeAdapter;

/**
 * @author huangyong
 * createTime 2019-09-18
 */
public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.setting)
    RecyclerView setting;
    @VisibleForTesting
    List<Object> items;
    @BindView(R.id.backup)
    ImageView backup;
    @BindView(R.id.center_tv)
    TextView centerTv;
    @BindView(R.id.right_view)
    FrameLayout rightView;
    @BindView(R.id.toolbar_layout)
    Toolbar toolbarLayout;
    @BindView(R.id.exit)
    TextView exit;
    private DownloadManager manager;

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        ButterKnife.bind(this);

        centerTv.setText("??????");

        MultiTypeAdapter adapter = new MultiTypeAdapter();

        adapter.register(TextItemSection.class, new TextItemSectionViewBinder());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        setting.setLayoutManager(linearLayoutManager);
        setting.setAdapter(adapter);

        items = new ArrayList<>();
        String userToken = UserUtil.getUserToken(this);
        if (TextUtils.isEmpty(userToken)) {
            exit.setVisibility(View.INVISIBLE);
        } else {
            exit.setVisibility(View.VISIBLE);
            items.add(new TextItemSection("????????????", () -> {
                    UserProfileActivity.start(SettingActivity.this);
            }));
        }
        String[] arr = {"IjkPlayer??????","ExoPlayer??????","MediaPlayer??????"};
        items.add(new TextItemSection("???????????????", () -> {
            CenterListPopupView listPopupView = new XPopup.Builder(this).asCenterList("???????????????", arr, new OnSelectListener() {
                @Override
                public void onSelect(int position, String text) {
                    switch (position) {
                        case 0:
                            PlayApp.swich(PlayApp.PLAN_ID_IJK);
                            SharePreferencesUtil.setIntSharePreferences(SettingActivity.this,DataInter.KEY.PLAY_CODEC,0);
                            break;
                        case 1:
                            PlayApp.swich(PlayApp.PLAN_ID_EXO);
                            SharePreferencesUtil.setIntSharePreferences(SettingActivity.this,DataInter.KEY.PLAY_CODEC,1);
                            break;
                        case 2:
                            PlayApp.swich(PlayApp.PLAN_ID_MEDIA);
                            SharePreferencesUtil.setIntSharePreferences(SettingActivity.this,DataInter.KEY.PLAY_CODEC,2);
                            break;
                    }
                }
            });
            listPopupView.show();
            int preferences = SharePreferencesUtil.getIntSharePreferences(this, DataInter.KEY.PLAY_CODEC, 0);
            listPopupView.setCheckedPosition(preferences);
        }));
        items.add(new TextItemSection("????????????", () -> {
            new XPopup.Builder(this).asConfirm("?????????", "???????????????????????????????????????????????????????????????????????????????????????????????????", new OnConfirmListener() {
                @Override
                public void onConfirm() {
                    DataCleanManager.cleanInternalCache(SettingActivity.this);
                    UserUtil.exitLogin(SettingActivity.this);
                    sendBroadcast(new Intent(DataInter.KEY.ACTION_REFRESH_COIN));
                }
            }).show();
        }));
        items.add(new TextItemSection("????????????", () -> {
            AboutUsActivity.start(SettingActivity.this);
        }));
        items.add(new TextItemSection("????????????", () -> {
//
            new UpdatePresenter(new IUpdate() {
                @Override
                public void loadDone(UpdateDto dto) {
                    exit.post(new Runnable() {
                        @Override
                        public void run() {
                            checkVersion(dto);

                        }
                    });
                }

                @Override
                public void loadEmpty() {

                }
            }).getUpdate();
        }));
        adapter.setItems(items);
        adapter.notifyDataSetChanged();


        exit.setOnClickListener(v -> new XPopup.Builder(SettingActivity.this).asConfirm("??????!", "?????????????????????", new OnConfirmListener() {
            @Override
            public void onConfirm() {
                UserUtil.exitLogin(SettingActivity.this);
                sendBroadcast(new Intent(DataInter.KEY.ACTION_EXIT_LOGIN));
                finish();
            }
        }).show());
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void checkVersion(UpdateDto dto) {
        /*
         * ??????????????????????????????
         * ?????????
         */
        UpdateConfiguration configuration = new UpdateConfiguration()
                //??????????????????
                .setEnableLog(true)
                //????????????????????????
                //.setHttpManager()
                //????????????????????????????????????
                .setJumpInstallPage(true)
                //??????????????????????????? (??????????????????demo???????????????)
                //.setDialogImage(R.drawable.ic_dialog)
                //?????????????????????
                //.setDialogButtonColor(Color.parseColor("#E743DA"))
                //???????????????????????????
                .setDialogButtonTextColor(Color.WHITE)
                //?????????????????????????????????
                .setShowNotification(true)
                //??????????????????????????????toast
                .setShowBgdToast(false)
                //??????????????????
                .setForcedUpgrade(false)
                //????????????????????????????????????
                .setButtonClickListener(new OnButtonClickListener() {
                    @Override
                    public void onButtonClick(int id) {

                    }
                })
                //???????????????????????????
                .setOnDownloadListener(new OnDownloadListener() {
                    @Override
                    public void start() {

                    }

                    @Override
                    public void downloading(int max, int progress) {

                    }

                    @Override
                    public void done(File apk) {

                    }

                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void error(Exception e) {

                    }
                });

        manager = DownloadManager.getInstance(this);

        manager.setApkName("????????????.apk")
                .setApkUrl(dto.getData().getDownloadUrl())
                .setSmallIcon(R.mipmap.ticon2)
                .setShowNewerToast(true)
                .setConfiguration(configuration)
//                .setDownloadPath(Environment.getExternalStorageDirectory() + "/AppUpdate")
                .setApkVersionCode(dto.getData().getVersionCode())
                .setApkVersionName(dto.getData().getVersion())
                .setApkSize("20.4")
                .setAuthorities(getPackageName())
                .setApkDescription(dto.getData().getUpdateMsg())
                .download();
        Window dialogWindow = manager.getDefaultDialog().getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (ScreenUtil.getWith(this) * 0.8f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);

    }
}
