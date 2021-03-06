package com.test.backend.app.requestparser;

import com.test.backend.server.endpoint.RequestParser;
import com.test.backend.server.endpoint.RequestParserException;
import com.test.backend.server.http.Request;

import java.util.List;

/**
 * Created by ulises on 10/10/15.
 */
public class HighScoreRequestParser implements RequestParser<Integer> {

    @Override
    public Integer parse(Request request) throws RequestParserException {
        List<String> pathParams = request.getPathParams().getVariables();
        if (pathParams.size() < 1) {
            throw new RequestParserException("Need to provide levelid");
        } else {
            String levelId = pathParams.get(0);
            return Integer.valueOf(levelId);
        }
    }
}
