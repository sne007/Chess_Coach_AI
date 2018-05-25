package org.empyrn.darkknight;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.nemesis.materialchess.R;

import org.empyrn.darkknight.gamelogic.ChessController;
import org.empyrn.darkknight.gamelogic.ChessParseError;
import org.empyrn.darkknight.gamelogic.GameTree;
import org.empyrn.darkknight.gamelogic.Move;
import org.empyrn.darkknight.gamelogic.PgnToken;
import org.empyrn.darkknight.gamelogic.Position;
import org.empyrn.darkknight.gamelogic.TextIO;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.empyrn.darkknight.EditBoard.EDIT_DIALOG;
import org.empyrn.darkknight.engine.ComputerPlayer;

public class Generation_Activity extends AppCompatActivity{

    Random rand = new Random();
    ChessBoard cb;
    int theme,choice;
    private SharedPreferences sharedPreferences;
    private DrawerLayout mDrawerLayout;
    ChessController ctrl = null;
    ChessTastic.PgnScreenText gameTextListener;
    private PGNOptions pgnOptions = new PGNOptions();
    ChessTastic ct = new ChessTastic();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_generation);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Generation_Activity.this);
        theme = sharedPreferences.getInt("Theme",R.style.AppThemeGrey);
        super.setTheme(theme);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            choice = sharedPreferences.getInt("Choice",4);
            switch(choice)
            {
                default:getWindow().setStatusBarColor(getResources().getColor(R.color.darkgrey));break;
            }
        }
        SpannableString s = new SpannableString("PRACTICE");
        s.setSpan(new TypefaceSpan(this, "KlinicSlabBold.otf"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Toolbar tl = (Toolbar) findViewById(R.id.my_awesome_toolbar2);
        if(tl != null){
            setSupportActionBar(tl);
            getSupportActionBar().setTitle(s);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        cb = (ChessBoard) findViewById(R.id.chessboard);
        cb.setFocusable(true);
        cb.requestFocus();
        cb.setClickable(true);
        fenGenerate();
        initUI();
    }

    private final void initUI() {
        cb = (ChessBoard)findViewById(R.id.chessboard);
        Button nb = (Button)findViewById(R.id.next_button);
        Button play = (Button)findViewById(R.id.play);
        nb.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(View v) {
                fenGenerate();
            }
        });
        play.setOnClickListener(new android.view.View.OnClickListener(){
            public void onClick(View v) {
                sendBackResult();
            }
        });

        cb.setFocusable(true);
        cb.requestFocus();
        cb.setClickable(true);

    }


    private final void sendBackResult() {
        if (checkValid()) {
            setPosFields();
            String fen = TextIO.toFEN(cb.pos);
            setResult(RESULT_OK, (new Intent()).setAction(fen));
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }
    private final void setPosFields() {
        setEPFile(getEPFile()); // To handle sideToMove change
        TextIO.fixupEPSquare(cb.pos);
        TextIO.removeBogusCastleFlags(cb.pos);
    }
    private final void setEPFile(int epFile) {
        int epSquare = -1;
        if ((epFile >= 0) && (epFile < 8)) {
            int epRank = cb.pos.whiteMove ? 5 : 2;
            epSquare = Position.getSquare(epFile, epRank);
        }
        cb.pos.setEpSquare(epSquare);
    }

    private final int getEPFile() {
        int epSquare = cb.pos.getEpSquare();
        if (epSquare < 0) return 8;
        return Position.getX(epSquare);
    }

    private void fenGenerate(){

        String fen = createFen();
            try {
                Position pos = TextIO.readFEN(fen);
                cb.setPosition(pos);
            } catch (ChessParseError e){
                if (e.pos != null)
                    cb.setPosition(e.pos);
            }
            cb.setSelection(-1);
            checkValid();
    }

    private String createFen() {
        char[][] grid = new char[8][8];

        placeKings(grid);
        placePieces(grid, "PPPPPPPP", true);
        placePieces(grid, "pppppppp", true);
        placePieces(grid, "RNBQBNR", false);
        placePieces(grid, "rnbqbnr", false);

        return toFen(grid);
    }

    private void placeKings(char[][] grid) {
        int r1, c1, r2, c2;
        while (true) {
            r1 = rand.nextInt(8);
            c1 = rand.nextInt(8);
            r2 = rand.nextInt(8);
            c2 = rand.nextInt(8);
            if (r1 != r2 && Math.abs(r1 - r2) > 1 && Math.abs(c1 - c2) > 1)
                break;
        }
        grid[r1][c1] = 'K';
        grid[r2][c2] = 'k';
    }

    private void placePieces(char[][] grid, String pieces, boolean isPawn) {
        int numToPlace = rand.nextInt(pieces.length());
        for (int n = 0; n < numToPlace; n++) {
            int r, c;
            do {
                r = rand.nextInt(8);
                c = rand.nextInt(8);

            } while (grid[r][c] != 0 || (isPawn && (r == 7 || r == 0)));

            grid[r][c] = pieces.charAt(n);
        }
    }

    private String toFen(char[][] grid) {
        StringBuilder fen = new StringBuilder();
        int countEmpty = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                char ch = grid[r][c];
                if (ch == 0) {
                    countEmpty++;
                } else {
                    if (countEmpty > 0) {
                        fen.append(countEmpty);
                        countEmpty = 0;
                    }
                    fen.append(ch);
                }
            }
            if (countEmpty > 0) {
                fen.append(countEmpty);
                countEmpty = 0;
            }
            fen.append("/");
        }
        return fen.append(" w - - 0 1").toString();
    }


    /** Test if a position is valid. */
    private final boolean checkValid() {
        try {
            String fen = TextIO.toFEN(cb.pos);
            TextIO.readFEN(fen);
            return true;
        } catch (ChessParseError e) {
            Toast.makeText(this, e.getMessage().toString(),Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}