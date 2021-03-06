package com.exlibris.restapis;

import org.apache.log4j.Logger;

public class RequestApi {

    final private static Logger logger = Logger.getLogger(RequestApi.class);

    public static HttpResponse createRequest(String mmsId, String holdingId, String itemId, String baseurl,
            String apiKey, String body, String userId) {

        logger.info("Starting to handle creating Request: " + body + ".");
        logger.info("Item Id: " + itemId + " - calling POST");

        String url = baseurl + "/almaws/v1/bibs/" + mmsId + "/holdings/" + holdingId + "/items/" + itemId
                + "/requests?user_id=" + userId + "&apikey=" + apiKey;
        HttpResponse bibResponse = AlmaRestUtil.sendHttpReq(url, "POST", body);

        return bibResponse;
    }

    public static HttpResponse getRequest(String mmsId, String holdingId, String itemId, String requestId,
            String baseurl, String apiKey) {

        logger.info("Starting to handle get Request Id: " + requestId + ".");
        logger.info("item Id: " + itemId + " - calling GET");

        String url = baseurl + "/almaws/v1/bibs/" + mmsId + "/holdings/" + holdingId + "/items/" + itemId + "/requests/"
                + requestId + "?apikey=" + apiKey;
        HttpResponse requestResponse = AlmaRestUtil.sendHttpReq(url, "GET", null);

        return requestResponse;
    }

    public static HttpResponse createBibRequest(String mmsId, String baseUrl, String apiKey, String body,
            String userId) {
        logger.info("Starting to handle creating Request: " + body + ".");
        logger.info("Mms Id: " + mmsId + " - calling POST");

        String url = baseUrl + "/almaws/v1/bibs/" + mmsId + "/requests?user_id=" + userId + "&apikey=" + apiKey;
        HttpResponse bibResponse = AlmaRestUtil.sendHttpReq(url, "POST", body);

        return bibResponse;
    }



}
