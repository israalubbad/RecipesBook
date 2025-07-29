package com.example.recipesbook.Activitys;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.recipesbook.Adapter.ViewPagerAdapter;
import com.example.recipesbook.Fragment.FollowListFragment;
import com.example.recipesbook.R;
import com.example.recipesbook.databinding.ActivityFollowListBinding;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
/*
 * FollowList Activity
 *
 * Activity responsible for displaying the user's followers and following lists
 * in a tabbed interface using TabLayout and ViewPager.
 *
 * Features:
 * - Dynamically loads "Following" and "Followers" fragments.
 * - Selects the appropriate tab based on intent extras.
 *
 */
public class FollowList extends AppCompatActivity {
ActivityFollowListBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityFollowListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TabLayout();

        Intent intent=getIntent();

        if(intent.getIntExtra("follow",0)==0) {
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0));
        }else{
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1));
        }


        binding.backIV.setOnClickListener(v->{finish();});
    }

    private void TabLayout() {
        ArrayList<String> tabs = new ArrayList<>();
        ArrayList<Fragment> fragments = new ArrayList<>();
        tabs.add("Following");
        fragments.add(FollowListFragment.newInstance("Following"));
        tabs.add("Followers");
        fragments.add(FollowListFragment.newInstance("Followers"));


        ViewPagerAdapter adapter = new ViewPagerAdapter(FollowList.this, fragments);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(tabs.get(position));
        }).attach();
    }
}