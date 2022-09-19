package com.brian19109.weatherapi.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brian19109.weatherapi.R;
import com.brian19109.weatherapi.adapter.ProjectListAdapter;
import com.brian19109.weatherapi.database.ProjectViewModel;

import java.util.concurrent.ExecutionException;

public class ProjectListFragment extends Fragment {
    private ProjectViewModel mProjectViewModel;
    private RecyclerView mRecyclerViewProjectList;
    private NoProjectFragment mNoProjectFragment;

    public ProjectListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProjectViewModel = new ViewModelProvider(this).get(ProjectViewModel.class);
        mNoProjectFragment = new NoProjectFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_list, container, false);

        mRecyclerViewProjectList = view.findViewById(R.id.recyclerview_project_list);

        if (getActivity() != null) {
            TextView tvBack = getActivity().findViewById(R.id.tv_back_on_action_bar_project_list);
            TextView tvAddProject = getActivity().findViewById(R.id.tv_add_project_on_action_bar_project_list);
            EditText etSearchProject = view.findViewById(R.id.et_search_project);

            getAllProjects();

            tvBack.setOnClickListener(v -> getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, new ExploreFragment(null)).commit());

            tvAddProject.setOnClickListener(v -> {
                if (getActivity() instanceof AppCompatActivity && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setCustomView(R.layout.action_bar_editing_project);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, new ProjectEditingFragment()).commit();
                }
            });

            etSearchProject.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (TextUtils.isEmpty(editable.toString().trim())) {
                        getAllProjects();
                    } else {
                        searchProjects(editable.toString());
                    }
                }
            });
        }

        return view;
    }

    public void getAllProjects() {
        if (mRecyclerViewProjectList != null) {
            mProjectViewModel.getAllProjects().observe(getViewLifecycleOwner(), projects -> {
                if (projects.size() == 0) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.constraintLayout_no_project, new NoProjectFragment()).commit();
                    mRecyclerViewProjectList.setVisibility(View.GONE);
                } else {
                    getActivity().getSupportFragmentManager().beginTransaction().hide(mNoProjectFragment).commit();
                    mRecyclerViewProjectList.setVisibility(View.VISIBLE);
                    final ProjectListAdapter adapter = new ProjectListAdapter(ProjectListFragment.this, new ProjectListAdapter.ProjectDiff(), ProjectListFragment.this);
                    mRecyclerViewProjectList.setAdapter(adapter);
                    mRecyclerViewProjectList.setLayoutManager(new LinearLayoutManager(getActivity()));
                    adapter.submitList(projects);
                }
            });
        }
    }

    private void searchProjects(String partialProjectName) {
        if (mRecyclerViewProjectList != null) {
            try {
                mProjectViewModel.getAllProjectsByPartialName(partialProjectName).observe(getViewLifecycleOwner(), projects -> {
                    if (projects.size() == 0) {
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.constraintLayout_no_project, mNoProjectFragment).commit();
                        mRecyclerViewProjectList.setVisibility(View.GONE);
                    } else {
                        getActivity().getSupportFragmentManager().beginTransaction().hide(mNoProjectFragment).commit();
                        mRecyclerViewProjectList.setVisibility(View.VISIBLE);
                        final ProjectListAdapter adapter = new ProjectListAdapter(ProjectListFragment.this, new ProjectListAdapter.ProjectDiff(), ProjectListFragment.this);
                        mRecyclerViewProjectList.setAdapter(adapter);
                        mRecyclerViewProjectList.setLayoutManager(new LinearLayoutManager(getActivity()));
                        adapter.submitList(projects);
                    }
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
