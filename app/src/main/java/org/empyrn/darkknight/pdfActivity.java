package org.empyrn.darkknight;

import com.nemesis.materialchess.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.empyrn.darkknight.gamelogic.ChessController;
import org.empyrn.darkknight.gamelogic.Move;
import org.empyrn.darkknight.gamelogic.Position;

import java.util.List;


public class pdfActivity extends Activity implements GUIInterface{
    String move = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        String s = getIntent().getStringExtra("move list");
        move = s;

        TextView tv = (TextView)findViewById(R.id.pdf_text);
        tv.setText(weakness(move));

    }


    public String weakness(String s){
        System.out.println(s);
        StringBuilder sb = new StringBuilder("");
        int i = s.length();
        String s1 = s.replaceAll("=","");
        String s2 = s1.replaceAll(">","");
        String s3 = s2.replaceAll("<","");

        String[] str = s3.split("\\\n");
        int len = str.length,j=1;

        while(j<len){

            for(; j<len; j++){
                if(str[j].length()>0){
                    if(str[j].charAt(0) == '1'){
                        continue;
                    }
                    else{
                        sb.append(str[j-1]);
                        break;
                    }
                }
            }
            int count = 0;

            for(; j<len; j++){
                if(str[j].length()>0)
                if(str[j].charAt(0) != '*'){
                    if(str[j].charAt(0) == 'd' && str[j].charAt(2) == '1' && count<1){
                        String[] str1 = str[j].split("\\s+");
                        sb.append("\n"+str1[5]+"\n");
                        count++;
                    }
                    else if(str[j].charAt(0) == '1'){
                        break;
                    }
                }
            }
        }

//        System.out.println(sb);
        StringBuilder sb1 = new StringBuilder("");
        String[] finals = sb.toString().split("\\\n");
        double d2=0,count_goodMove=0,count_totalMoves=0;
        sb1.append(finals[0]);

        for(int m = 2;m<finals.length;m++){
            if(m%2 == 0)
                sb1.append(finals[m]);
            else if(m%2 != 0 && (!finals[m].contains("m"))){
                d2 = Double.valueOf(finals[m]);
                if(d2 < 0.5){
                    sb1.append("\ngood move\n");
                    count_goodMove++;
                    count_totalMoves++;
                }
                else if(d2 < 2 && d2>0.5){
                    sb1.append("\nInaccuracy!\n");
                    count_totalMoves++;
                }
                else if(d2 > 4 && d2<8){
                    sb1.append("\nA blunder\n");
                    count_totalMoves++;
                }
                else if(d2 > 8){
                    sb1.append("\nYou're in a very bad position!\n");
                    count_totalMoves++;
                }
            }
            else if(finals[m].contains("m")){
                sb1.append("\nyou will loose in atmost "+finals[m].charAt(1)+" moves,given the opponent plays right moves\n");
                count_totalMoves++;
                break;
            }
        }
        sb1.append("\nYour move accuracy:"+(count_goodMove/count_totalMoves)*100+"\n");
        return sb1.toString();
    }

    @Override
    public void setPosition(Position pos, String variantInfo, List<Move> variantMoves) {

    }

    @Override
    public void setSelection(int sq) {

    }

    @Override
    public void setStatusString(String str) {

    }

    @Override
    public void moveListUpdated() {

    }

    @Override
    public void setThinkingInfo(String pvStr, String bookInfo, List<Move> pvMoves, List<Move> bookMoves) {

    }

    @Override
    public void requestPromotePiece() {

    }

    @Override
    public void runOnUIThread(Runnable runnable) {

    }

    @Override
    public void reportInvalidMove(Move m) {

    }

    @Override
    public void computerMoveMade() {

    }

    @Override
    public void humanMoveMade(Move m) {

    }

    @Override
    public void setRemainingTime(long wTime, long bTime, long nextUpdate) {

    }
}