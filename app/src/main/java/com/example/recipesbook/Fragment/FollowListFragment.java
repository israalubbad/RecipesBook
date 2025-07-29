package com.example.recipesbook.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.recipesbook.Activitys.UserProfile;
import com.example.recipesbook.Adapter.UsersAdapter;
import com.example.recipesbook.Model.Users;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.FragmentFollowListBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
/*
 * FollowListFragment
 *
 * A Fragment that displays a list of users based on the follow type:
 * either "Following" (users that the current user follows)
 * or "Followers" (users who follow the current user).
 *
 * Features:
 * - Retrieves the appropriate list (following or followers) from Firestore.
 * - Loads user data for each ID in the list.
 * - Handles user click events to navigate to their profile screen.
 *
 */

public class FollowListFragment extends Fragment implements UsersAdapter.OnClickListener {

    private static final String ARG_FOLLOW = "follow";
    FirebaseFirestore fireStore;
    private String follow;
    List<Users> users;
    FragmentFollowListBinding binding;

    public FollowListFragment() {
        // Required empty public constructor
    }


    public static FollowListFragment newInstance(String follow) {
        FollowListFragment fragment = new FollowListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FOLLOW, follow);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            follow = getArguments().getString(ARG_FOLLOW);
        }
        users = new ArrayList<>();
        fireStore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFollowListBinding.inflate(inflater, container, false);

        String collectionName;

        if(follow.equals("Following")){
            collectionName="following";
        }else{
            collectionName ="followers";
        };

        showListOfFollow(collectionName);

        return binding.getRoot();
    }

    private void showListOfFollow(String collectionName) {
        fireStore.collection("users").document(Utils.USER_ID)
                .collection(collectionName)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> docs = task.getResult().getDocuments();
                        if (docs.isEmpty()) {
                            Toast.makeText(getContext(), "No users found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DocumentSnapshot doc : docs) {
                            String userId = doc.getId();
                            fireStore.collection("users").document(userId).get()
                                    .addOnSuccessListener(userSnap -> {
                                        Users user = userSnap.toObject(Users.class);
                                        if (user != null) {
                                            user.setUserId(userId);
                                            users.add(user);
                                        }

                                        if (users.size() == docs.size()) {
                                            binding.recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
                                            UsersAdapter adapter = new UsersAdapter(users, FollowListFragment.this);
                                            binding.recyclerview.setAdapter(adapter);


                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    public void onClick(String userId) {
        Intent intent = new Intent(getContext(), UserProfile.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }
}