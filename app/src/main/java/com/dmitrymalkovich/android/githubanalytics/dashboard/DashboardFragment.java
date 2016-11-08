package com.dmitrymalkovich.android.githubanalytics.dashboard;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.dmitrymalkovich.android.githubanalytics.R;
import com.dmitrymalkovich.android.githubanalytics.traffic.TrafficActivity;
import com.dmitrymalkovich.android.githubanalytics.welcome.WelcomeActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.dmitrymalkovich.android.githubanalytics.traffic.TrafficActivity.EXTRA_REPOSITORY_ID;
import static com.google.common.base.Preconditions.checkNotNull;

public class DashboardFragment extends Fragment implements DashboardContract.View {

    private DashboardContract.Presenter mPresenter;
    private Unbinder unbinder;
    ProgressBar mProgressBar;
    @BindView(R.id.empty_state)
    View mEmptyStateView;
    @BindView(R.id.recycler_view_for_repositories)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DashboardListAdapter mAdapter;

    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        unbinder = ButterKnife.bind(this, root);

        int columnCount = getResources().getInteger(R.integer.grid_column_count);
        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);

        mAdapter = new DashboardListAdapter(null, this);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(mPresenter);

        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle(R.string.navigation_view_dashboard);
            }
            mProgressBar = (ProgressBar) getActivity().findViewById(R.id.progress);
        }

        mPresenter.start(savedInstanceState);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void setPresenter(DashboardContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(active ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void openUrl(@NonNull String htmlUrl) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(htmlUrl));
        getActivity().startActivity(browserIntent);
    }

    @Override
    public void setRefreshIndicator(boolean active) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(active);
        }
    }

    @Override
    public void showRepositories(Cursor data) {
        this.mAdapter.swapCursor(data);
    }

    @Override
    public void signOut() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void showTraffic(long id) {
        Intent intent = new Intent(getActivity(), TrafficActivity.class);
        intent.putExtra(EXTRA_REPOSITORY_ID, id);
        startActivity(intent);
    }
}
