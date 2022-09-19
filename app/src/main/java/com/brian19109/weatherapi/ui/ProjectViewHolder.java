package com.brian19109.weatherapi.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.brian19109.weatherapi.R;
import com.brian19109.weatherapi.database.ProjectViewModel;

public class ProjectViewHolder extends RecyclerView.ViewHolder {
    private final TextView mProjectItemView;

    public ProjectViewHolder(@NonNull View itemView) {
        super(itemView);
        mProjectItemView = itemView.findViewById(R.id.tv_project_name);
    }

    public static ProjectViewHolder create(ViewGroup parent, @NonNull ViewModelStoreOwner owner, @NonNull Fragment parentFragment) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_project_item, parent, false);

        TextView tvProjectName = view.findViewById(R.id.tv_project_name);
        ImageView ivDelete = view.findViewById(R.id.iv_trash_can_delete);
        ImageView ivProjectNavigation = view.findViewById(R.id.iv_project_navigation);

        ivDelete.setOnClickListener(v -> {
            ProjectViewModel projectViewModel = new ViewModelProvider(owner).get(ProjectViewModel.class);
            projectViewModel.deleteProject(tvProjectName.getText().toString().trim());
            if (parentFragment instanceof ProjectListFragment) {
                ((ProjectListFragment)parentFragment).getAllProjects();
            }
        });

        ivProjectNavigation.setOnClickListener(v -> {
            if (parentFragment.getActivity() != null) {
                ExploreFragment exploreFragment = new ExploreFragment(tvProjectName.getText().toString().trim());
                parentFragment.getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, exploreFragment).commit();
            }
        });

        return new ProjectViewHolder(view);
    }

    public void bind(String text) {
        mProjectItemView.setText(text);
    }
}
