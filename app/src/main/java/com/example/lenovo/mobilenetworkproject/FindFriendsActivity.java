package com.example.lenovo.mobilenetworkproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton searchButton;
    private EditText searchInputText;

    private RecyclerView searchResultList;

    private DatabaseReference allUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");


        mToolbar = findViewById(R.id.find_friends_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        searchResultList = findViewById(R.id.search_result_list);
        searchResultList.setHasFixedSize(true);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));

        searchButton = findViewById(R.id.search_peeople_friends_button);
        searchInputText = findViewById(R.id.search_box_input);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchBoxInput = searchInputText.getText().toString();
               SearchPeopleAndFriends(searchBoxInput);
            }
        });


    }
    //11.dk 37.video

    private void SearchPeopleAndFriends(String searchBoxInput)
    {

        Toast.makeText(this, "Searching..", Toast.LENGTH_LONG).show();
        Query searchPeopleandFriendsQuery = allUsersDatabaseRef.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff"); //unique order

        FirebaseRecyclerOptions<FindFriends> options = new FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(searchPeopleandFriendsQuery, FindFriends.class).build();
        FirebaseRecyclerAdapter<FindFriends,FindFriendsViewHolder > firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull FindFriends model)
            {
                Picasso.get().load(model.getProfileimage()).placeholder(R.drawable.profile).into(holder.myImage);
                holder.myName.setText(model.getFullname());
                holder.myStatus.setText(model.getStatus());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        String visit_user_id = getRef(position).getKey();
                        Intent profileIntent = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout, viewGroup, false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };
        searchResultList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        private CircleImageView myImage;
        private TextView myName, myStatus;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            myImage = itemView.findViewById(R.id.all_users_profile_image);
            myName = itemView.findViewById(R.id.all_users_profile_full_name);
            myStatus = itemView.findViewById(R.id.all_users_status);

        }
    }
}
