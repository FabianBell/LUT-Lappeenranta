package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Question extends AppCompatActivity {

    //Elements
    private TextView mQuestionText;
    private Button mAnswer1Button;
    private Button mAnswer2Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        mQuestionText = findViewById(R.id.questionText);
        mAnswer1Button = findViewById(R.id.answer1Button);
        mAnswer2Button = findViewById(R.id.answer2Button);

        //get Context
        String question = getIntent().getStringExtra("QUESTION");
        final String answer1 = getIntent().getStringExtra("ANSWER1");
        final String answer2 = getIntent().getStringExtra("ANSWER2");
        final ArrayList<String> param = new ArrayList<>();
        final String base = "EXTRA";
        for (int i = 1; getIntent().getStringExtra(base + i) != null; i++){
            param.add(getIntent().getStringExtra(base + 1));
        }

        mQuestionText.setText(question);
        mAnswer1Button.setText(answer1);
        mAnswer2Button.setText(answer2);

        mAnswer1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("ANSWER", "1");
                if (param != null){
                    for (int i = 1; i <= param.size(); i++){
                        data.putExtra(base + i, param.get(i-1));
                    }
                }
                setResult(RESULT_OK, data);
                finish();
            }
        });

        mAnswer2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("ANSWER", "2");
                if (param != null){
                    for (int i = 1; i < param.size()-1; i++){
                        data.putExtra(base + i, param.get(i-1));
                    }
                }
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }
}
