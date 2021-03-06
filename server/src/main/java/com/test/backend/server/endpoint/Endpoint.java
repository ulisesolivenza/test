package com.test.backend.server.endpoint;

import com.test.backend.server.http.*;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ulises on 8/10/15.
 */
public abstract class Endpoint<T> {

    private final ResponseBuilder responseBuilder;
    private final URIMatcher uriMatcher;
    private final HttpMethod httpMethod;
    private final RequestParser<T> requestParser;

    public Endpoint(String pathExpression, ResponseBuilder responseBuilder, HttpMethod httpMethod, RequestParser<T> requestParser) {
        this.uriMatcher = new RegexURIMatcher(pathExpression);
        this.responseBuilder = responseBuilder;
        this.httpMethod = httpMethod;
        this.requestParser = requestParser;
    }

    private final Logger log = Logger.getLogger(Endpoint.class.getName());

    public abstract Response doCall(Request request, T parsedRequest) throws Exception;

    public Response call(Request request) {
        try {

            T requestObject = requestParser.parse(request);
            return doCall(request, requestObject);

        } catch (RequestParserException e) {
            log.log(Level.ALL,e.getLocalizedMessage(),e);
            return newResponse(HttpStatus.BAD_REQUEST, request, e.getMessage());

        } catch (Exception e) {
            log.log(Level.ALL,e.getLocalizedMessage(),e);
            return newServerErrorResponse(request, e.getMessage());
        }

    }

    public boolean match(URI requestUri, String method) {
        if (method.equals(httpMethod.name())) {
            return uriMatcher.match(requestUri);
        }
        return false;
    }


    protected Response newResponse(HttpStatus status, Request request, String response) {
        return responseBuilder.newResponse(status, request, response);
    }

    protected Response newServerErrorResponse(Request request, String response) {
        return responseBuilder.newServerErrorResponse(request, response);
    }
}
