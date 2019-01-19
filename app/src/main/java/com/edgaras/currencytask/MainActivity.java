package com.edgaras.currencytask;

import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.edgaras.currencytask.Connection.DetectConnection;
import com.edgaras.currencytask.Connection.GetData;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnRefresh, btnExit, btnCloseExchangeResult;
    private Button btnFromPrevious, btnFromNext, btnToPrevious, btnToNext, btnCount;
    private LinearLayout noConnectionLayout;
    private ConstraintLayout exchangeResultLayout, mainLayout;
    private TextView txtLoadingInfo, txtBalance, txtToCurrency, txtConvertedFromAmount, txtConvertedToAmount, txtCommissionFeeAmount, txtTotalFeesPaidAmount;
    private EditText amountInput;
    private User appUser;
    private int fromCurrencyIndex = 0;
    private int toCurrencyIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideNavigation();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = (ConstraintLayout) findViewById(R.id.mainLayout);
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyboard(amountInput);
                hideNavigation();
                return false;
            }
        });

        noConnectionLayout = (LinearLayout) findViewById(R.id.noConnectionLayout);
        exchangeResultLayout = (ConstraintLayout) findViewById(R.id.exchangeResultLayout);
        txtLoadingInfo = (TextView) findViewById(R.id.txtLoadingInfo);
        txtBalance = (TextView) findViewById(R.id.txtBalance);
        txtToCurrency = (TextView) findViewById(R.id.txtToCurrency);
        txtConvertedFromAmount = (TextView) findViewById(R.id.txtConvertedFromAmount);
        txtConvertedToAmount = (TextView) findViewById(R.id.txtConvertedToAmount);
        txtCommissionFeeAmount = (TextView) findViewById(R.id.txtCommissionFeeAmount);
        txtTotalFeesPaidAmount = (TextView) findViewById(R.id.txtTotalFeesPaidAmount);

        amountInput = (EditText) findViewById(R.id.amountInput);
        amountInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                hideKeyboard(amountInput);
                hideNavigation();
                return false;
            }
        });
        amountInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0){
                    String text = s.toString();
                    if(s.length() > 0 && s.charAt(0) == '.'){
                        amountInput.setText(text.substring(0, text.length() - 1));
                        amountInput.setSelection(amountInput.getText().length());
                    }
                    if(s.length() > 1 && s.charAt(0) == '0' && s.charAt(1) != '.'){
                        amountInput.setText(text.substring(0, text.length() - 1));
                        amountInput.setSelection(amountInput.getText().length());
                    }
                    if (text.contains(".") && text.substring(text.indexOf(".") + 1).length() > 2) {
                        amountInput.setText(text.substring(0, text.length() - 1));
                        amountInput.setSelection(amountInput.getText().length());
                    }
                }
            }
        });

        btnRefresh = (ImageButton) findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUserData();
            }
        });

        btnExit = (ImageButton) findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });

        btnFromPrevious = (Button) findViewById(R.id.btnFromPrevious);
        btnFromPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPreviousFromCurrency();
            }
        });

        btnFromNext = (Button) findViewById(R.id.btnFromNext);
        btnFromNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectNextFromCurrency();
            }
        });

        btnToPrevious = (Button) findViewById(R.id.btnToPrevious);
        btnToPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPreviousToCurency();
            }
        });

        btnToNext = (Button) findViewById(R.id.btnToNext);
        btnToNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectNextToCurrency();
            }
        });

        btnCount = (Button) findViewById(R.id.btnExchange);
        btnCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(amountInput.getText().length() > 0){
                    if(DetectConnection.checkInternetConnection(getApplicationContext())){
                        disableUIButtons();
                        exchangeCurrency();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.noConnection, Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), R.string.amountMissing, Toast.LENGTH_LONG).show();
                }
            }
        });

        btnCloseExchangeResult = (ImageButton) findViewById(R.id.btnCloseExchangeResult);
        btnCloseExchangeResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableUIButtons();
                exchangeResultLayout.setVisibility(View.GONE);
            }
        });

        getUserData();
    }

    private void getUserData() {
        if(DetectConnection.checkInternetConnection(getApplicationContext()))
        {
            // isivaizduojam, kad paemem vartotojo saskaitos duomenis is serverio
            List<Currency> userCurrencies = new ArrayList<>();
            Currency eur = new Currency("EUR", 1000.0, 0, 0.0);
            Currency usd = new Currency("USD", 0.0, 0, 0.0);
            Currency jpy = new Currency("JPY", 0.0, 0, 0.0);

            userCurrencies.add(eur);
            userCurrencies.add(usd);
            userCurrencies.add(jpy);

            appUser = new User(userCurrencies);
            updateBalanceView(0, 0);

            enableUIButtons();
            noConnectionLayout.setVisibility(View.GONE);
        } else {
            txtLoadingInfo.setText(R.string.noConnection);
            noConnectionLayout.setVisibility(View.VISIBLE);
        }
    }

    private void selectPreviousFromCurrency(){
        if(fromCurrencyIndex - 1 >= 0){
            fromCurrencyIndex--;
        } else {
            fromCurrencyIndex = appUser.getWallet().size()-1;
        }

        updateBalanceView(fromCurrencyIndex, 0);
        selectNextToCurrency();
    }
    private void selectNextFromCurrency(){
        if(fromCurrencyIndex + 1 < appUser.getWallet().size()){
            fromCurrencyIndex++;
        } else {
            fromCurrencyIndex = 0;
        }

        updateBalanceView(fromCurrencyIndex, 0);
        selectNextToCurrency();
    }

    private void updateBalanceView(int currencyIndex, int baseCurrencyIndex){
        if(appUser.getWallet().size() > 0 && appUser.getWallet().size() > currencyIndex){
            txtBalance.setText(String.format("%.2f",appUser.getWallet().get(currencyIndex).getBalanceLeft()) + " " + appUser.getWallet().get(currencyIndex).getShortName());
            txtTotalFeesPaidAmount.setText(String.format("%.2f",appUser.getWallet().get(currencyIndex).getTotalSpentOnCommissionFees()) + " " + appUser.getWallet().get(baseCurrencyIndex).getShortName());
        }
    }

    private void selectPreviousToCurency(){
        if(toCurrencyIndex - 1 >= 0){
            toCurrencyIndex--;
        } else {
            toCurrencyIndex = appUser.getWallet().size()-1;
        }

        if(toCurrencyIndex != fromCurrencyIndex){
            updateToCurrencyView(toCurrencyIndex);
        } else {
            selectPreviousToCurency();
        }
    }
    private void selectNextToCurrency(){
        if(toCurrencyIndex + 1 < appUser.getWallet().size()){
            toCurrencyIndex++;
        } else {
            toCurrencyIndex = 0;
        }

        if(toCurrencyIndex != fromCurrencyIndex){
            updateToCurrencyView(toCurrencyIndex);
        } else {
            selectNextToCurrency();
        }
    }

    private void updateToCurrencyView(int currencyIndex){
        if(appUser.getWallet().size() > 0 && appUser.getWallet().size() > currencyIndex){
            txtToCurrency.setText(appUser.getWallet().get(currencyIndex).getShortName());
        }
    }

    private void exchangeCurrency(){
        double amountToExhange = Double.parseDouble(amountInput.getText().toString());

        if(amountToExhange > appUser.getWallet().get(fromCurrencyIndex).getBalanceLeft()){
            Toast.makeText(getApplicationContext(), R.string.notEnoughMoney, Toast.LENGTH_LONG).show();
            enableUIButtons();
        } else {
            double commissionFeePercentage = appUser.getWallet().get(toCurrencyIndex).getCommissionFee();
            double commissionFeeAmount = amountToExhange * commissionFeePercentage;

            if(appUser.getWallet().get(fromCurrencyIndex).getBalanceLeft() - amountToExhange - commissionFeeAmount >= 0){
                double amountReceived = GetData.getExchangeAmount(amountToExhange, appUser.getWallet().get(fromCurrencyIndex).getShortName(), appUser.getWallet().get(toCurrencyIndex).getShortName());

                if(amountReceived != 0){
                    appUser.getWallet().get(fromCurrencyIndex).takeFromWallet(amountToExhange);
                    appUser.getWallet().get(toCurrencyIndex).addToWallet(amountReceived);
                    appUser.getWallet().get(toCurrencyIndex).addToTotalSpentOnCommissionFees(commissionFeeAmount);
                    appUser.getWallet().get(toCurrencyIndex).addToTransactionsCount(1);

                    txtConvertedFromAmount.setText(String.format("%.2f", amountToExhange) + " " + appUser.getWallet().get(fromCurrencyIndex).getShortName());
                    txtConvertedToAmount.setText(String.format("%.2f", amountReceived) + " " + appUser.getWallet().get(toCurrencyIndex).getShortName());
                    txtCommissionFeeAmount.setText(String.format("%.2f", commissionFeeAmount) + " " + appUser.getWallet().get(fromCurrencyIndex).getShortName());
                    exchangeResultLayout.setVisibility(View.VISIBLE);

                    amountInput.setText("");

                    updateBalanceView(fromCurrencyIndex, 0);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.unknownError, Toast.LENGTH_LONG).show();
                    enableUIButtons();
                }

            } else {
                Toast.makeText(getApplicationContext(), R.string.notEnoughMoneyForFees, Toast.LENGTH_LONG).show();
                enableUIButtons();
            }
        }
    }

    private void enableUIButtons(){
        btnFromPrevious.setEnabled(true);
        btnFromNext.setEnabled(true);
        btnToPrevious.setEnabled(true);
        btnToNext.setEnabled(true);
        btnCount.setEnabled(true);
        amountInput.setEnabled(true);
    }
    private void disableUIButtons(){
        btnFromPrevious.setEnabled(false);
        btnFromNext.setEnabled(false);
        btnToPrevious.setEnabled(false);
        btnToNext.setEnabled(false);
        btnCount.setEnabled(false);
        amountInput.setEnabled(false);
    }

    private void hideKeyboard(EditText textInput){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
        textInput.clearFocus();
    }
    private void hideNavigation(){
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
