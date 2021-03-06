package com.example.mywallet.services;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.example.mywallet.R;
import com.example.mywallet.models.CurrencyBalance;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Nomics service.
 */
public class NomicsService {

    private static final String NOMICS_URL = "https://api.nomics.com/v1/";
    private static final String NOMICS_API_KEY = ApplicationService.getAppContext().getString(R.string.nomic_api_key);
    private static final List<String> urlCache = new ArrayList<>();

    private static class CryptoCurrenciesConversionsRequest extends Request<Map<String, Double>> {

        private final Response.Listener<Map<String, Double>> listener;
        private final Map<String, Double> balances;

        /**
         * Instantiates a new Crypto currencies conversions request.
         *
         * @param balances              the balances
         * @param convertCurrencySymbol the convert currency symbol
         * @param responseListener      the response listener
         */
        public CryptoCurrenciesConversionsRequest(Map<String, Double> balances, String convertCurrencySymbol, Response.Listener<Map<String, Double>> responseListener) {
            super(Request.Method.GET, NOMICS_URL + "currencies/ticker?key=" + NOMICS_API_KEY + "&format=json&convert="
                    + convertCurrencySymbol + "&ids=" + balances.keySet().toString().substring(1, balances.keySet().toString().length() - 1)
                    + "&interval=none", error -> System.out.println("error"));
            this.balances = balances;
            listener = responseListener;
        }

        @Override
        protected Response<Map<String, Double>> parseNetworkResponse(NetworkResponse response) {

            Map<String, Double> prices = new HashMap<>();
            String jsonAsString;

            try {
                jsonAsString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                JSONArray jsonArray = new JSONArray(jsonAsString);
                for (int i = 0 ; i != jsonArray.length() ; i++) {
                    prices.put(jsonArray.getJSONObject(i).getString("id"),
                            Double.parseDouble(jsonArray.getJSONObject(i).getString("price"))
                            * balances.get(jsonArray.getJSONObject(i).getString("id")));
                }
                return Response.success(prices, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException | JSONException e) {
                Log.e("getPrices", "Error while converting currencies : " + e);
                e.printStackTrace();
                return Response.error(new ParseError(e));
            }

        }

        @Override
        protected void deliverResponse(Map<String, Double> response) {
            listener.onResponse(response);
        }

    }

    private static class CryptoInfoRequest extends Request<List<CurrencyBalance>> {

        private final Response.Listener<List<CurrencyBalance>> listener;
        private final Map<String, Double> balances;
        private final String convertCurrencySymbol;

        /**
         * Instantiates a new Crypto info request.
         *
         * @param balances              the balances
         * @param convertCurrencySymbol the convert currency symbol
         * @param responseListener      the response listener
         */
        public CryptoInfoRequest(Map<String, Double> balances, String convertCurrencySymbol, Response.Listener<List<CurrencyBalance>> responseListener) {
            super(Request.Method.GET, NOMICS_URL + "currencies/ticker?key=" + NOMICS_API_KEY + "&format=json&convert="
                    + convertCurrencySymbol + "&ids=" + balances.keySet().toString().substring(1, balances.keySet().toString().length() - 1)
                    + "&interval=none", error -> System.out.println("error"));
            this.balances = balances;
            listener = responseListener;
            this.convertCurrencySymbol = convertCurrencySymbol;
        }

        @Override
        protected Response<List<CurrencyBalance>> parseNetworkResponse(NetworkResponse response) {

            List<CurrencyBalance> currencyBalances = new ArrayList<>();
            String jsonAsString;

            try {
                jsonAsString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                JSONArray jsonArray = new JSONArray(jsonAsString);
                for (int i = 0 ; i != jsonArray.length() ; i++) {
                    currencyBalances.add(
                            new CurrencyBalance(
                                    jsonArray.getJSONObject(i).getString("id"),
                                    balances.get(jsonArray.getJSONObject(i).getString("id")),
                                    Double.valueOf(jsonArray.getJSONObject(i).getString("price")),
                                    convertCurrencySymbol,
                                    jsonArray.getJSONObject(i).getString("name")
                                    )
                    );
                }
                return Response.success(currencyBalances, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException | JSONException e) {
                Log.e("getPrices", "Error while getting crypto info : " + e);
                e.printStackTrace();
                return Response.error(new ParseError(e));
            }

        }

        @Override
        protected void deliverResponse(List<CurrencyBalance> response) {
            listener.onResponse(response);
        }

    }

    /**
     * Retrieve and put the consumption data into the database between two dates
     *
     * @param balances         The balances you need to know the price of
     * @param endAsset         The asset into which you want to convert your input assets
     * @param responseListener the response listener
     */
    public static void getPrices(Map<String, Double> balances , String endAsset,
                                                Response.Listener<Map<String, Double>> responseListener) {

        CryptoCurrenciesConversionsRequest cryptoCurrenciesConversionsRequest =
                new CryptoCurrenciesConversionsRequest(balances, endAsset, responseListener);

        RequestQueue requestQueue = Volley.newRequestQueue(ApplicationService.getAppContext());
        requestQueue.add(cryptoCurrenciesConversionsRequest);

    }

    /**
     * Gets crypto infos.
     *
     * @param balances         the balances
     * @param endAsset         the end asset
     * @param responseListener the response listener
     */
    public static void getCryptoInfos(Map<String, Double> balances , String endAsset,
                                      Response.Listener<List<CurrencyBalance>> responseListener) {

        CryptoInfoRequest cryptoInfoRequest =
                new CryptoInfoRequest(balances, endAsset, responseListener);

        RequestQueue requestQueue = Volley.newRequestQueue(ApplicationService.getAppContext());
        requestQueue.add(cryptoInfoRequest);

    }

}
