package com.ericksonvaz.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  {

    private TextView txtResult;
    private String stringToEvalute = "";
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = findViewById(R.id.linerWithButtons);

        View rootView = this.getWindow().getDecorView().getRootView();

        ArrayList<View> touchables = rootView.getTouchables();
        txtResult = findViewById(R.id.textResult);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.onBtnClick((Button) v);
            }
        };

        for (View v: touchables) {
            // TODO: omit button if that common listener isn't meant for it
            if(v instanceof Button) {
                v.setOnClickListener(listener);
            }
        }
    }

    protected final void onBtnClick(Button btn){
        String btnStringId = btn.getResources().getResourceEntryName(btn.getId());

        Log.d("ValueEnter", String.valueOf(btn.getText()));

        switch (btn.getId()){
            case R.id.btn0:
            case R.id.btn1:
            case R.id.btn2:
            case R.id.btn3:
            case R.id.btn4:
            case R.id.btn5:
            case R.id.btn6:
            case R.id.btn7:
            case R.id.btn8:
            case R.id.btn9:
                if(stringToEvalute.equals("0")||stringToEvalute.equals("0.0")){
                    stringToEvalute="";
                }
                stringToEvalute +=(btn.getText());
                txtResult.setText(stringToEvalute);
                break;
            case R.id.btnClear:
                stringToEvalute = "0";
                txtResult.setText("0");
                break;
            case R.id.btnPlus:
            case R.id.btnMin:
            case R.id.btnMult:
            case R.id.btnDiv:
                if(
                        stringToEvalute.equals("0")||
                                stringToEvalute.equals("")||
                                stringToEvalute.equals(" ")){
                    stringToEvalute="0";
                    txtResult.setText(stringToEvalute);
                }
                if(
                        !stringToEvalute.endsWith("+")||
                                !stringToEvalute.endsWith("-")||
                                !stringToEvalute.endsWith("/")||
                                !stringToEvalute.endsWith("*")
                ){
                    if(btn.getText().equals("x")){
                        stringToEvalute +=(" * ");
                    }else {
                        stringToEvalute +=(" "+btn.getText()+" ");
                    }

                    txtResult.setText(stringToEvalute);
                }
                break;
            case R.id.btnEq:
                if(
                        stringToEvalute.equals("0")||
                        stringToEvalute.equals("")||
                        stringToEvalute.equals(" ")){
                    stringToEvalute="0";
                }else{
                    stringToEvalute = stringToEvalute.trim();
                    if(
                            stringToEvalute.endsWith("+")||
                            stringToEvalute.endsWith("-")||
                            stringToEvalute.endsWith("/")||
                            stringToEvalute.endsWith("*")
                    ){
                        stringToEvalute += "0";
                    }else{

                        Log.d("String to evalute", stringToEvalute);
                        txtResult.setText(String.valueOf(eval(stringToEvalute)));
                        stringToEvalute = String.valueOf(txtResult.getText());
                    }

                }

                break;
        }
    }

    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }
}