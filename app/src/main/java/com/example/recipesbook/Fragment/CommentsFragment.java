package com.example.recipesbook.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.recipesbook.Adapter.CommentsAdapter;
import com.example.recipesbook.Model.Comments;
import com.example.recipesbook.Utils;
import com.example.recipesbook.databinding.FragmentCommentsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
/*
 * CommentsFragment
 *
 * Fragment responsible for displaying and managing user comments
 * on recipe details.
 *
 * Features:
 * - Loads comments related to a specific recipe from Firestore.
 * - Allows the current user to post new comments and deleted their own comments.
 * - Automatically refreshes the comments list when new comments are added.
 * - Sends a notification to the recipe owner when a new comment is posted.
 *
 */

public class CommentsFragment extends Fragment {
    private static final String ARG_RECIPEID = "recipeId";
    FirebaseFirestore firestore;
    FragmentCommentsBinding binding;
    List<Comments> commentsList;
    private String recipeId;

    public CommentsFragment() {
        // Required empty public constructor
    }

    public static CommentsFragment newInstance(String recipeId) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RECIPEID, recipeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipeId = getArguments().getString(ARG_RECIPEID);
        }
        firestore = FirebaseFirestore.getInstance();
        commentsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCommentsBinding.inflate(getLayoutInflater(), container, false);

        loadComments();
        AddComment();



        return binding.getRoot();
    }

    private void AddComment() {
        firestore.collection("recipe").document(recipeId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String ownerId = documentSnapshot.getString("userId");
                        binding.sendButton.setOnClickListener(v -> {
                            String commentUser = binding.commentEditText.getText().toString();
                            if (commentUser.isEmpty()) {
                                Toast.makeText(getContext(), "Please enter a comment", Toast.LENGTH_SHORT).show();
                            } else {
                                String timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

                                String commentId = firestore.collection("recipe").document(recipeId)
                                        .collection("comments").document().getId();

                                Comments comments = new Comments(commentId, Utils.USER_ID, recipeId, commentUser, timeStamp);

                                firestore.collection("recipe").document(recipeId).collection("comments")
                                        .document(commentId).set(comments)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "Comment added successfully", Toast.LENGTH_SHORT).show();
                                                binding.commentEditText.setText("");

                                                //Add notification
                                                if (!ownerId.equals(Utils.USER_ID)) {
                                                    firestore.collection("users").document(Utils.USER_ID).get()
                                                            .addOnSuccessListener(doc -> {
                                                                String name = doc.getString("name");
                                                                Utils.sendNotificationToUser(ownerId, recipeId, Utils.USER_ID, "New Comment", name + " commented on your recipe.");
                                                            });
                                                }

                                                loadComments();
                                            } else {
                                                Toast.makeText(getContext(), "Error adding comment", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    }
                });
    }

    private void loadComments() {
        commentsList.clear();
        firestore.collection("recipe").document(recipeId).collection("comments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            Comments comment = doc.toObject(Comments.class);
                            commentsList.add(comment);
                        }
                        CommentsAdapter adapter = new CommentsAdapter(commentsList);
                        binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        binding.commentsRecyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Failed to load comments", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
