package com.example.recipesbook.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.recipesbook.Activitys.NotificationList;
import com.example.recipesbook.R;
import com.example.recipesbook.Adapter.ViewPagerAdapter;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.FragmentHomeBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {
    FirebaseAuth auth;
    FirebaseFirestore fireStore;
    ArrayList<Fragment> fragments;
    ArrayList<String> tabs;
    FragmentHomeBinding binding;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";



    public HomeFragment() {

    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

        fireStore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        tabs = new ArrayList<>();
        fragments = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        String text = "Make your own food,\n stay at home";
        SpannableString spannable = new SpannableString(text);
        int start = text.indexOf("home");
        int end = start + "home".length();
        spannable.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.orange_dark)), // color
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        loadData();

        tabLayout();

        binding.notification.setOnClickListener(v -> {
            Intent intent =new Intent(getContext(), NotificationList.class);
            startActivity(intent);
        });

        return binding.getRoot();
    }

    private void tabLayout() {
        tabs.add("All");
        fragments.add(ViewRecipeFragment.newInstance());

        List<String> category = Arrays.asList(getResources().getStringArray(R.array.meal_categories));
        for (String cat : category) {
            tabs.add(cat);
            fragments.add(CategoryFragment.newInstance(cat));
        }

        if (isAdded()) {
            ViewPagerAdapter adapter = new ViewPagerAdapter(requireActivity(), fragments);
            binding.viewPager.setAdapter(adapter);

            new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
                tab.setText(tabs.get(position));
            }).attach();
        }

        for (int i = 0; i < binding.tabLayout.getTabCount(); i++) {
            View tab = ((ViewGroup) binding.tabLayout.getChildAt(0)).getChildAt(i);
            if (tab != null) {
                ViewGroup.MarginLayoutParams margin = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
                margin.setMargins(12, 0, 12, 0);
                tab.getTextAlignment();
                tab.requestLayout();
            }
        }

    }

    private void loadData() {
        fireStore.collection("users").document(Utils.USER_ID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                binding.nameTv.append( task.getResult().getString("name") + "!");
                String imageUrl = task.getResult().getString("imageUrl");
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.profile_image)
                        .into(binding.userImageIV);
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }


}