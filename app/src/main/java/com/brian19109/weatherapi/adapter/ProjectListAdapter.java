package com.brian19109.weatherapi.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.brian19109.weatherapi.model.Project;
import com.brian19109.weatherapi.ui.ProjectViewHolder;

public class ProjectListAdapter extends ListAdapter<Project, ProjectViewHolder> {
    private Fragment mParentFragment;
    private ViewModelStoreOwner mViewModelStoreOwner;

    public ProjectListAdapter(@NonNull ViewModelStoreOwner owner, @NonNull DiffUtil.ItemCallback<Project> diffCallback, @NonNull Fragment parentFragment) {
        super(diffCallback);
        mParentFragment = parentFragment;
        mViewModelStoreOwner = owner;
    }

    @Override
    public ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ProjectViewHolder.create(parent, mViewModelStoreOwner, mParentFragment);
    }

    @Override
    public void onBindViewHolder(ProjectViewHolder holder, int position) {
        Project current = getItem(position);
        holder.bind(current.getProjectName());
    }

    public static class ProjectDiff extends DiffUtil.ItemCallback<Project> {

        @Override
        public boolean areItemsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
            return oldItem.getProjectName().equals(newItem.getProjectName());
        }
    }
}
