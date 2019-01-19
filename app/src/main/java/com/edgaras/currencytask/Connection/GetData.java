package com.edgaras.currencytask.Connection;

import org.json.JSONObject;

public class GetData {

    public static Double getExchangeAmount(Double amountToExhange, String fromCurrency, String toCurrency)
    {
        String myUrl = "http://api.evp.lt/currency/commercial/exchange/" + amountToExhange + "-" + fromCurrency + "/" + toCurrency + "/latest";
        String result = "";
        HttpGetRequest getRequest = new HttpGetRequest();

        try
        {
            result = getRequest.execute(myUrl).get();
            if(result != null)
            {
                JSONObject jsonObj = new JSONObject(result);
                String amountReceived = jsonObj.get("amount").toString();

                return Double.parseDouble(amountReceived);
            }
            else return 0.0;
        }
        catch (Exception e)
        {
            return 0.0;
        }
    }
}
