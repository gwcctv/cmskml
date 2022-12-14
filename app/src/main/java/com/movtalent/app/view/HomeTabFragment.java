package com.movtalent.app.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.movtalent.app.R;
import com.movtalent.app.adapter.BannerEntity;
import com.movtalent.app.adapter.BannerEntityViewBinder;
import com.movtalent.app.adapter.CategoryViewBinder;
import com.movtalent.app.adapter.FooterViewViewBinder;
import com.movtalent.app.adapter.ItemVideosViewBinder;
import com.movtalent.app.model.Category;
import com.movtalent.app.model.FooterView;
import com.movtalent.app.model.dto.HomeLevelDto;
import com.movtalent.app.model.dto.VideoListDto;
import com.movtalent.app.model.vo.CommonVideoVo;
import com.movtalent.app.presenter.HomePresenter;
import com.movtalent.app.presenter.iview.IHomeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.bakumon.statuslayoutmanager.library.DefaultOnStatusChildClickListener;
import me.bakumon.statuslayoutmanager.library.StatusLayoutManager;
import me.drakeet.multitype.MultiTypeAdapter;

/**
 * @author huangyong
 * createTime 2019-09-11
 */
public class HomeTabFragment extends Fragment implements IHomeView {


    @BindView(R.id.home_rv)
    RecyclerView homeRv;
    Unbinder unbinder;
    private MultiTypeAdapter adapter;
    @VisibleForTesting
    List<Object> items;
    private StatusLayoutManager statusLayoutManager;
    private HomePresenter homePresenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_fragment_layout, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {


        statusLayoutManager = new StatusLayoutManager.Builder(homeRv)
                .setDefaultLayoutsBackgroundColor(Color.WHITE)
                .setDefaultEmptyClickViewTextColor(getResources().getColor(R.color.colorPrimary))
                .setLoadingLayout(R.layout.loading_layout)
                .setEmptyLayout(R.layout.empty_layout)
                .setDefaultEmptyText("??????????????????????????????")
                .setOnStatusChildClickListener(new DefaultOnStatusChildClickListener() {
                    @Override
                    public void onEmptyChildClick(View view) {
                        statusLayoutManager.showLoadingLayout();
                        homePresenter.getHomeLevel();
                    }

                    @Override
                    public void onErrorChildClick(View view) {
                        statusLayoutManager.showLoadingLayout();
                        homePresenter.getHomeLevel();
                    }
                })
                .build();
        statusLayoutManager.showLoadingLayout();

        final GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                Object item = items.get(position);
                return (item instanceof BannerEntity || item instanceof Category || item instanceof FooterView) ? 3 : 1;
            }
        };
        adapter = new MultiTypeAdapter();

        layoutManager.setSpanSizeLookup(spanSizeLookup);
        homeRv.setLayoutManager(layoutManager);

        //?????????????????????view
        adapter.register(BannerEntity.class, new BannerEntityViewBinder());
        adapter.register(Category.class, new CategoryViewBinder());
        adapter.register(VideoListDto.DataBean.class, new ItemVideosViewBinder());
        adapter.register(FooterView.class, new FooterViewViewBinder());
       /*
       //????????????
       adapter.register(HomeAdEntity.class, new HomeAdEntityViewBinder());

       */

        homeRv.setAdapter(adapter);

        items = new ArrayList<>();

        items.add(new BannerEntity());
        homePresenter = new HomePresenter(this);
        homePresenter.getHomeLevel();

        homePresenter.getHomeBannerLevel(6,1,10);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void loadDone(HomeLevelDto videoVos) {
        statusLayoutManager.showSuccessLayout();
        if (videoVos.getData() != null) {
            if (videoVos.getData().getLe1() != null && videoVos.getData().getLe1().size() > 0) {
                items.add(new Category("???????????????????????????", v -> {
                    AllLevelActivity.startTo(getContext(), 1);
                }));
                items.addAll(videoVos.getData().getLe1());
            }


            if (videoVos.getData().getLe2() != null && videoVos.getData().getLe2().size() > 0) {
                items.add(new Category("???????????????????????????", v -> {
                    AllLevelActivity.startTo(getContext(), 2);
                }));
                items.addAll(videoVos.getData().getLe2());
            }


            if (videoVos.getData().getLe3() != null && videoVos.getData().getLe3().size() > 0) {
                items.add(new Category("???????????????", v -> {
                    AllLevelActivity.startTo(getContext(), 3);
                }));
                items.addAll(videoVos.getData().getLe3());
            }



            if (videoVos.getData().getLe4() != null && videoVos.getData().getLe4().size() > 0) {
                items.add(new Category("????????????", v -> {
                    AllLevelActivity.startTo(getContext(), 4);
                }));
                items.addAll(videoVos.getData().getLe4());
            }

            if (videoVos.getData().getLe5() != null && videoVos.getData().getLe5().size() > 0) {
                items.add(new Category("????????????", v -> {
                    AllLevelActivity.startTo(getContext(), 5);
                }));
                items.addAll(videoVos.getData().getLe5());
            }

            items.add(new FooterView("???????????????"));

            adapter.setItems(items);
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), "????????????????????????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void loadDone(ArrayList<CommonVideoVo> videoVos) {

    }

    @Override
    public void loadMore(ArrayList<CommonVideoVo> videoVos) {

    }

    @Override
    public void loadBanner(ArrayList<CommonVideoVo> videoVos) {
        if (items!=null&&items.size()>0){
            BannerEntity bannerEntity = (BannerEntity) items.get(0);
            if (bannerEntity!=null){
                bannerEntity.setVideoVos(videoVos);
                adapter.notifyItemChanged(0);
            }
        }
    }

    @Override
    public void loadError() {
        Toast.makeText(getContext(), "????????????????????????????????????", Toast.LENGTH_SHORT).show();
        statusLayoutManager.showErrorLayout();
    }

    @Override
    public void loadEmpty() {

    }
}
