package com.fabianbell.janinakeller.lut_lappeenranta;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Question extends AppCompatActivity {

    //Elements
    private TextView mQuestionText;
    private Button mAnswer1Button;
    private Button mAnswer2Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        mQuestionText = (TextView) findViewById(R.id.questionText);
        mAnswer1Button = (Button) findViewById(R.id.answer1Button);
        mAnswer2Button = (Button) findViewById(R.id.answer2Button);

        //get Context
        String question = getIntent().getStringExtra("QUESTION");
        final String answer1 = getIntent().getStringExtra("ANSWER1");
        final String answer2 = getIntent().getStringExtra("ANSWER2");

        mQuestionText.setText(question);
        mAnswer1Button.setText(answer1);
        mAnswer2Button.setText(answer2);

        mAnswer1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("ANSWER", "1");
                setResult(RESULT_OK, data);
                finish();
            }
        });

        mAnswer2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("ANSWER", "2");
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }
}
