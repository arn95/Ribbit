package com.ribbit.android.Activities;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.app.ToolbarActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.ribbit.R;
import com.ribbit.android.ParseObjects.Post;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;
import it.gmariotti.cardslib.library.view.CardViewNative;
import it.gmariotti.cardslib.library.view.base.CardViewWrapper;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.main_recycle_card_view) CardRecyclerView cardRecyclerView;
    @Bind(R.id.main_toolbar) Toolbar toolbar;

    ArrayList<Card> cardList = new ArrayList<>();
    ArrayList<Post> postImages = new ArrayList<>();
    CardArrayRecyclerViewAdapter cardArrayRecyclerViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        this.getSupportActionBar().setTitle("Ribbit");

        cardArrayRecyclerViewAdapter = new CardArrayRecyclerViewAdapter(this, cardList);
        cardRecyclerView.setHasFixedSize(false);
        cardRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        //Set the empty view
        if (cardRecyclerView != null) {
            cardRecyclerView.setAdapter(cardArrayRecyclerViewAdapter);
        }

        fetchPosts();
    }

    public void fetchPosts(){
        //fetching images
        ParseQuery query = new ParseQuery("Post");
        query.whereEqualTo("type", "image");
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List list, ParseException e) {
                for (Object imageObject : list) {
                    Post postObject = (Post) imageObject;
                    CardViewNative cardViewNative = new CardViewNative(MainActivity.this);
                    CardHeader cardHeader = new CardHeader(MainActivity.this);
                    cardHeader.setTitle("Test Post");
                    CardThumbnail cardThumbnail = new CardThumbnail(MainActivity.this);
                    cardThumbnail.setUrlResource(postObject.getFile().getUrl());
                    Card card = new Card(MainActivity.this);
                    card.addCardHeader(cardHeader);
                    card.addCardThumbnail(cardThumbnail);
                    cardViewNative.setCard(card);
                    cardList.add(card);
                }
                cardArrayRecyclerViewAdapter.notifyDataSetChanged();
                e.printStackTrace();
            }

            @Override
            public void done(Object o, Throwable throwable) {

            }
        });
        //TODO: Fetching vids
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        //disable back button press
        //super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_media) {
            startActivity(new Intent(MainActivity.this, CameraActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
